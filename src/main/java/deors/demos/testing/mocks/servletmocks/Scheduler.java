package deors.demos.testing.mocks.servletmocks;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

/**
 * Task scheduler.
 *
 * The tasks are read from an INI configuration file. Each task appears as a section in the INI
 * file, and the task name is the section name. Inside a section, four entries configures the task:
 *
 * <ol>
 * <li><code>class</code> is the fully qualified name of the class that implements the task.</li>
 * <li><code>description</code> is the description used when showing task information.</li>
 * <li><code>start</code> is the task start time in HH:MM:SS format.</li>
 * <li><code>stop</code> is the task stop time in HH:MM:SS format.</li>
 * </ol>
 *
 * When the task start time equals the string <code>*</code> (the configurable daemon id string)
 * the task is then a daemon, and does not stop until the task itself ends.
 *
 * The tasks are implemented extending the abstract class <code>SchedulerTask</code>.
 *
 * By default new tasks are loaded using the scheduler thread class loader, but it can
 * be configured to use any initialized class loader.
 *
 * @author deors
 * @version 1.0
 */
public final class Scheduler
    extends Thread {

    /**
     * The task list.
     *
     * @see Scheduler#getTasks()
     */
    private final List<SchedulerTask> tasks = new ArrayList<SchedulerTask>();

    /**
     * The scheduler thread.
     */
    @SuppressWarnings("PMD.AvoidUsingVolatile")
    volatile Thread schedulerThread;

    /**
     * The scheduler class loader. Its initial value is the thread class loader
     * as returned by the method <code>getContextClassLoader()</code> of
     * <code>Thread</code> class.
     */
    private ClassLoader schedulerClassLoader = this.getContextClassLoader();

    /**
     * The date formatter.
     */
    private SimpleDateFormat dateFormatter;

    /**
     * String that identifies a daemon task. Configurable in the properties file using the key
     * <code>sched.daemonId</code>. Default value is <code>*</code>.
     * default value.
     *
     * @see CommonsContext#getConfigurationProperty(String, String)
     */
    public static final String DAEMON_ID = "*"; //$NON-NLS-1$

    /**
     * Date format string used to print time information in the scheduler messages (not the same
     * that the time information printed by the default log). Configurable in the properties file
     * using the key <code>sched.dateFormat</code>. Default value is <code>yyyy/MM/dd HH:mm:ss</code>.
     *
     * @see CommonsContext#getConfigurationProperty(String, String)
     */
    private static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss"; //$NON-NLS-1$

    /**
     * The task class entry name.
     */
    private static final String TASK_CLASS_ENTRY_KEY = "class"; //$NON-NLS-1$

    /**
     * The task description entry name.
     */
    private static final String TASK_DESCRIPTION_ENTRY_KEY = "description"; //$NON-NLS-1$

    /**
     * The task start time entry name.
     */
    private static final String TASK_START_ENTRY_KEY = "start"; //$NON-NLS-1$

    /**
     * The task stop time entry name.
     */
    private static final String TASK_STOP_ENTRY_KEY = "stop"; //$NON-NLS-1$

    /**
     * The time token separator.
     */
    private static final String TIME_SEPARATOR = ":"; //$NON-NLS-1$

    /**
     * Scheduler thread sleep time.
     */
    private static final long SCHEDULER_SLEEP_TIME = 100;

    /**
     * The finalize guardian.
     */
    final Object finalizeGuardian = new Object() {

        /**
         * Finalizes the object by stopping the current running tasks and the scheduler process.
         *
         * @throws Throwable a throwable object
         *
         * @see Object#finalize()
         */
        protected void finalize()
            // CHECKSTYLE:OFF
            throws Throwable {
            // CHECKSTYLE:ON

            try {
                stopAllTasks();
            } finally {
                super.finalize();
            }
        }
    };

    /**
     * Default constructor. No tasks are scheduled.
     */
    public Scheduler() {

        super();

        dateFormatter = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        dateFormatter.setLenient(false);
    }

    /**
     * Constructor that sets the file that contains the tasks information.
     *
     * An <code>IllegalArgumentException</code> exception is thrown if a required key is
     * missing in the configuration file or a task class could not be successfully created or a task
     * start or stop time are not valid.
     *
     * @param iniFile the file with the tasks information
     *
     * @throws IOException an i/o exception
     */
    public Scheduler(File iniFile)
        throws IOException {

        this();

        // reads the configuration file
        INIFileManager ifm = new INIFileManager(iniFile);

        Iterator<String> sections = ifm.getSections().iterator();
        while (sections.hasNext()) {
            String taskName = sections.next();

            // the default section in the INI file is ignored
            if (taskName.length() == 0) {
                continue;
            }

            String taskClassName = readClassName(ifm, taskName);
            String taskDescription = readDescription(ifm, taskName);
            Calendar taskStartTime = readStartTime(ifm, taskName);
            Calendar taskStopTime = readStopTime(ifm, taskName);

            // the task is scheduled
            scheduleTask(taskName, taskClassName, taskDescription, taskStartTime, taskStopTime);
        }
    }

    /**
     * Reads the task class name.
     *
     * An <code>IllegalArgumentException</code> exception is thrown if the class name
     * is not found in the configuration file.
     *
     * @param ifm the configuration file manager
     * @param taskName the task name
     *
     * @return the task class name
     */
    private String readClassName(INIFileManager ifm, String taskName) {

        String taskClassName = ifm.getValue(taskName, TASK_CLASS_ENTRY_KEY);
        if (taskClassName == null) {
            throw new IllegalArgumentException("SCHED_ERR_KEY_CLASS_NOT_FOUND"); //$NON-NLS-1$
        }
        return taskClassName;
    }

    /**
     * Reads the task description.
     *
     * An <code>IllegalArgumentException</code> exception is thrown if the description
     * is not found in the configuration file.
     *
     * @param ifm the configuration file manager
     * @param taskName the task name
     *
     * @return the task description
     */
    private String readDescription(INIFileManager ifm, String taskName) {

        String taskDescription = ifm.getValue(taskName, TASK_DESCRIPTION_ENTRY_KEY);
        if (taskDescription == null) {
            throw new IllegalArgumentException("SCHED_ERR_KEY_DESCRIPTION_NOT_FOUND"); //$NON-NLS-1$
        }
        return taskDescription;
    }

    /**
     * Reads the task start time.
     *
     * An <code>IllegalArgumentException</code> exception is thrown if the start time
     * is not found in the configuration file or the value is not a valid time.
     *
     * @param ifm the configuration file manager
     * @param taskName the task name
     *
     * @return the task start time
     */
    private Calendar readStartTime(INIFileManager ifm, String taskName) {

        String tempStartTime = ifm.getValue(taskName, TASK_START_ENTRY_KEY);
        if (tempStartTime == null) {
            throw new IllegalArgumentException("SCHED_ERR_KEY_START_NOT_FOUND"); //$NON-NLS-1$
        }

        return parseStartTime(tempStartTime);
    }

    /**
     * Parses the task start time.
     *
     * @param timeString the task start time as a string
     *
     * @return the task start time
     */
    private Calendar parseStartTime(String timeString) {

        Calendar taskStartTime = null;
        try {
            taskStartTime = parseTime(timeString);
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException("SCHED_ERR_TASK_INVALID_START_TIME", iae); //$NON-NLS-1$
        }

        return taskStartTime;
    }

    /**
     * Reads the task stop time.
     *
     * An <code>IllegalArgumentException</code> exception is thrown if the stop time
     * is not found in the configuration file or the value is not a valid time.
     *
     * @param ifm the configuration file manager
     * @param taskName the task name
     *
     * @return the task stop time
     */
    private Calendar readStopTime(INIFileManager ifm, String taskName) {

        String tempStopTime = ifm.getValue(taskName, TASK_STOP_ENTRY_KEY);
        if (tempStopTime == null) {
            throw new IllegalArgumentException("SCHED_ERR_KEY_STOP_NOT_FOUND"); //$NON-NLS-1$
        }

        return parseStopTime(tempStopTime);
    }

    /**
     * Parses the task stop time.
     *
     * @param timeString the task stop time as a string
     *
     * @return the task stop time
     */
    private Calendar parseStopTime(String timeString) {

        Calendar taskStoptTime = null;
        try {
            taskStoptTime = parseTime(timeString);
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException("SCHED_ERR_TASK_INVALID_STOP_TIME", iae); //$NON-NLS-1$
        }

        return taskStoptTime;
    }

    /**
     * Constructor that sets the file that contains the tasks information using its name.
     *
     * An <code>IllegalArgumentException</code> exception is thrown if a required key is
     * missing in the configuration file or the task class could not be successfully created or a
     * task start or stop time are not valid.
     *
     * @param iniFileName the name of the file with the tasks information
     *
     * @throws IOException an i/o exception
     */
    public Scheduler(String iniFileName)
        throws IOException {

        this(new File(iniFileName));
    }

    /**
     * Starts the scheduler. The first command-line argument is the name of the file with the tasks
     * information.
     *
     * @param args the array of command-line arguments
     */
    public static void main(String[] args) {

        if (args.length != 1) {
            info("SCHED_LOG_PARAMETER_INI_FILE"); //$NON-NLS-1$
            return;
        }

        try {
            Scheduler sch = new Scheduler(args[0]);
            sch.startScheduler();
        } catch (IOException ioe) {
            info("SCHED_LOG_EXCEPTION_INI_FILE_MISSING"); //$NON-NLS-1$
            return;
        } catch (IllegalArgumentException iae) {
            info("SCHED_LOG_EXCEPTION_INI_FILE_INVALID"); //$NON-NLS-1$
            return;
        }
    }

    /**
     * Parses a string containing a time in HH:MM:SS format. If the string equals the value in
     * <code>DAEMON_ID</code> the method returns <code>null</code>. The resulting
     * <code>java.util.Calendar</code> object date is the current date and its time is the parsed
     * time.
     *
     * An <code>IllegalArgumentException</code> exception is thrown if the input string
     * is not valid.
     *
     * @param timeString the string to be parsed
     *
     * @return a <code>java.util.Calendar</code> object with the current date and the parsed time
     *         or <code>null</code> if the input string identifies a daemon task
     *
     * @see Scheduler#DAEMON_ID
     */
    static Calendar parseTime(String timeString) {

        if (timeString.equals(DAEMON_ID)) {
            return null;
        }

        try {
            java.util.StringTokenizer st = new java.util.StringTokenizer(timeString, TIME_SEPARATOR);

            Calendar retValue = Calendar.getInstance();

            retValue.set(Calendar.HOUR_OF_DAY, Integer.parseInt(st.nextToken()));
            retValue.set(Calendar.MINUTE, Integer.parseInt(st.nextToken()));
            retValue.set(Calendar.SECOND, Integer.parseInt(st.nextToken()));
            retValue.clear(Calendar.MILLISECOND);

            return retValue;
        } catch (NoSuchElementException nsee) {
            // the exception constructor has no parameters because is catched in the callers
            throw new IllegalArgumentException(nsee);
        } catch (NumberFormatException nfe) {
            // the exception constructor has no parameters because is catched in the callers
            throw new IllegalArgumentException(nfe);
        }
    }

    /**
     * Returns the task with the given name. If the task does not exist, the method returns
     * <code>null</code>.
     *
     * @return the task with the given name or <code>null</code> if a task with the given name
     *         does not exist
     *
     * @param taskName the task name
     *
     * @see Scheduler#tasks
     * @see Scheduler#existsTask(String)
     * @see Scheduler#getTasks()
     */
    public SchedulerTask getTask(String taskName) {

        synchronized (tasks) {
            for (SchedulerTask task : tasks) {
                if (task.getTaskName().equals(taskName)) {
                    return task;
                }
            }
        }
        return null;
    }

    /**
     * Returns the <code>task</code> property value.
     *
     * @return the property value
     *
     * @see Scheduler#tasks
     */
    public List<SchedulerTask> getTasks() {

        return tasks;
    }

    /**
     * Changes the scheduler class loader.
     *
     * @param schedulerClassLoader the new class loader instance
     *
     * @see Scheduler#schedulerClassLoader
     */
    public void setSchedulerClassLoader(ClassLoader schedulerClassLoader) {
        this.schedulerClassLoader = schedulerClassLoader;
    }

    /**
     * Runs the scheduler. The method checks the start and stop times for each task asking them to
     * start and stop in the scheduled times. When a task is stopped it is re-scheduled the same
     * time in the following day.
     */
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public void run() {

        Thread thisThread = Thread.currentThread();

        while (thisThread == schedulerThread) {
            Calendar now = Calendar.getInstance();

            for (SchedulerTask task : tasks) {
                if (task.isDaemonTask()) {
                    checkDaemonStart(task);
                    continue;
                }

                runChecks(now, task);
            }

            try {
                sleep(SCHEDULER_SLEEP_TIME);
            } catch (InterruptedException ie) {
                continue;
            }
        }

        if (schedulerThread == null) {
            stopAllTasks();
        }
    }

    /**
     * Runs checks needed during task execution: start, stop and reschedule task checks.
     *
     * @param now the current time
     * @param task the task
     */
    private void runChecks(Calendar now, SchedulerTask task) {

        checkRescheduleTask(now, task);
        checkStopTask(now, task);
        checkStartTask(now, task);
    }

    /**
     * Checks whether the task needs to be rescheduled.
     *
     * @param now the current time
     * @param task the task
     */
    private void checkRescheduleTask(Calendar now, SchedulerTask task) {

        if (task.getTaskNextStartTime() == null || task.getTaskNextStopTime() == null) {
            task.setTaskNextStartTime(task.getTaskStartTime());

            if (now.after(task.getTaskNextStartTime())) {
                task.getTaskNextStartTime().add(Calendar.DAY_OF_MONTH, 1);
            }

            task.setTaskNextStopTime(task.getTaskStopTime());

            if (task.getTaskNextStopTime().before(task.getTaskNextStartTime())) {
                task.getTaskNextStopTime().add(Calendar.DAY_OF_MONTH, 1);
            }

            info("SCHED_LOG_TASK_SCHEDULED"); //$NON-NLS-1$
        }
    }

    /**
     * Checks whether the task needs to be stopped.
     *
     * @param now the current time
     * @param task the task
     */
    private void checkStopTask(Calendar now, SchedulerTask task) {

        if (task.isExecuting()
            && !task.isStopping()
            && (now.equals(task.getTaskNextStopTime())
                || now.after(task.getTaskNextStopTime()))) {
            task.taskStop();
        }
    }

    /**
     * Checks whether the task needs to be started.
     *
     * @param now the current time
     * @param task the task
     */
    private void checkStartTask(Calendar now, SchedulerTask task) {

        if (!task.isStarting()
            && !task.isExecuting()
            && (now.equals(task.getTaskNextStartTime())
                || now.after(task.getTaskNextStartTime()))) {
            task.taskStart();
        }
    }

    /**
     * Checks whether a daemon task needs to be started.
     *
     * @param task the task
     */
    private void checkDaemonStart(SchedulerTask task) {

        if (!task.isStarting() && !task.isExecuting() && !task.isDaemonExecuted()) {
            info("SCHED_LOG_DAEMON_SCHEDULED"); //$NON-NLS-1$

            task.taskStart();
        }
    }

    /**
     * Starts the scheduler process.
     */
    public void startScheduler() {

        schedulerThread = new Thread(this);
        schedulerThread.start();
    }

    /**
     * Checks the existence of a task with the given name.
     *
     * @return whether a task with the given name exists
     *
     * @param taskName the task name
     */
    public boolean existsTask(String taskName) {

        synchronized (tasks) {
            for (SchedulerTask task : tasks) {
                if (task.getTaskName().equals(taskName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Kills a task. If the task does not exist or it is not running, the method does nothing.
     *
     * @param taskName the task name
     */
    public void killTask(String taskName) {

        synchronized (tasks) {
            for (SchedulerTask task : tasks) {
                if (task.getTaskName().equals(taskName)) {
                    if (task.isExecuting()) {
                        task.taskKill();
                    }
                    getTasks().remove(task);
                    break;
                }
            }
        }
    }

    /**
     * Schedules a new task or re-schedules an existing task. If the task exists and it is running,
     * the method does nothing.
     *
     * An <code>IllegalArgumentException</code> exception is thrown if the task class
     * could not be successfully created.
     *
     * @param taskName the task name
     * @param taskClass the task class
     * @param taskDescription the task description
     * @param taskStartTime the task start time
     * @param taskStopTime the task stop time
     */
    public void scheduleTask(String taskName, Class<?> taskClass, String taskDescription,
                             Calendar taskStartTime, Calendar taskStopTime) {

        synchronized (tasks) {
            if (rescheduleIfExist(taskName, taskStartTime, taskStopTime)) {
                return;
            }

            // the task is new
            if (taskClass == null || taskDescription == null || taskDescription.length() == 0) {
                throw new IllegalArgumentException("SCHED_ERR_TASK_INCOMPLETE"); //$NON-NLS-1$
            }

            try {
                SchedulerTask task =
                    (SchedulerTask) taskClass
                        .getConstructor(new Class[] {
                            String.class, String.class, Calendar.class, Calendar.class})
                        .newInstance(new Object[] {
                            taskName, taskDescription, taskStartTime, taskStopTime});

                if (taskStartTime == null) {
                    task.setDaemonTask(true);
                    task.setDaemonExecuted(false);
                }

                tasks.add(task);

            } catch (NoSuchMethodException nsme) {
                throw new IllegalArgumentException("SCHED_ERR_TASK_CLASS_INVALID", nsme); //$NON-NLS-1$
            } catch (InstantiationException ie) {
                throw new IllegalArgumentException("SCHED_ERR_TASK_CLASS_INVALID", ie); //$NON-NLS-1$
            } catch (IllegalAccessException iae) {
                throw new IllegalArgumentException("SCHED_ERR_TASK_CLASS_INVALID", iae); //$NON-NLS-1$
            } catch (InvocationTargetException ite) {
                throw new IllegalArgumentException("SCHED_ERR_TASK_CLASS_INVALID", ite); //$NON-NLS-1$
            }
        }
    }

    /**
     * Checks for the existence of a given task by name and re-schedules it.
     *
     * @param taskName the task name
     * @param taskStartTime the task start time
     * @param taskStopTime the task stop time
     *
     * @return <code>true</code> if the task existed and was rescheduled
     */
    private boolean rescheduleIfExist(String taskName, Calendar taskStartTime, Calendar taskStopTime) {

        for (SchedulerTask task : tasks) {
            if (task.getTaskName().equals(taskName)) {
                if (!task.isStarting() && !task.isExecuting()) {
                    if (taskStartTime == null) {
                        task.setTaskStartTime(null);
                        task.setTaskStopTime(null);
                        task.setTaskNextStartTime(null);
                        task.setTaskNextStopTime(null);
                        task.setDaemonTask(true);
                        task.setDaemonExecuted(false);
                    } else {
                        task.setTaskStartTime(taskStartTime);
                        task.setTaskStopTime(taskStopTime);
                        task.setTaskNextStartTime(null);
                        task.setTaskNextStopTime(null);
                        task.setDaemonTask(false);
                        task.setDaemonExecuted(false);
                    }
                }

                return true;
            }
        }

        return false;
    }

    /**
     * Schedules a new task or re-schedules an existing task. If the task exists and it is running,
     * the method does nothing. The class is loaded using the scheduler class loader.
     *
     * An <code>IllegalArgumentException</code> exception is thrown if the task class
     * could not be successfully created.
     *
     * @param taskName the task name
     * @param taskClassName the task class name
     * @param taskDescription the task description
     * @param taskStartTime the task start time
     * @param taskStopTime the task stop time
     *
     * @see Scheduler#schedulerClassLoader
     */
    public void scheduleTask(String taskName, String taskClassName, String taskDescription,
                             Calendar taskStartTime, Calendar taskStopTime) {

        synchronized (tasks) {
            if (taskClassName == null || taskClassName.length() == 0) {
                throw new IllegalArgumentException("SCHED_ERR_TASK_INCOMPLETE"); //$NON-NLS-1$
            }

            try {
                Class<?> taskClass = Class.forName(taskClassName, true, schedulerClassLoader);

                scheduleTask(taskName, taskClass, taskDescription, taskStartTime, taskStopTime);

            } catch (ClassNotFoundException cnfe) {
                throw new IllegalArgumentException("SCHED_ERR_TASK_NOT_FOUND", cnfe); //$NON-NLS-1$
            }
        }
    }

    /**
     * Stops and removes all tasks from the scheduling table and ends the scheduler thread.
     */
    public void stopAllTasks() {

        synchronized (tasks) {
            schedulerThread = null;
            for (SchedulerTask task : tasks) {
                if (task.isExecuting()) {
                    task.taskStop();
                }
            }
            getTasks().clear();
        }
    }

    /**
     * Stops a task and removes it from the scheduling table. If the task does not exist the method
     * does nothing. If the task exists but is not running, it is only removed.
     *
     * @param taskName the task name
     */
    public void stopAndRemoveTask(String taskName) {

        synchronized (tasks) {
            for (SchedulerTask task : tasks) {
                if (task.getTaskName().equals(taskName)) {
                    if (task.isExecuting()) {
                        task.taskStop();
                    }
                    getTasks().remove(task);
                    break;
                }
            }
        }
    }

    /**
     * Stops a task. If the task does not exist or it is not running, the method does nothing.
     *
     * @param taskName the task name
     */
    public void stopTask(String taskName) {

        synchronized (tasks) {
            for (SchedulerTask task : tasks) {
                if (task.getTaskName().equals(taskName)) {
                    if (task.isExecuting()) {
                        task.taskStop();
                    }
                    break;
                }
            }
        }
    }

    /**
     * Dummy log method.
     *
     * @param message the log message
     */
    private static void info(String message) {
        System.out.println(message);
    }
}
