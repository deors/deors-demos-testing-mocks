package deors.demos.testing.mocks.servletmocks;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

/**
 * Task scheduler.<br>
 *
 * The tasks are read from an INI configuration file. Each task appears as a section in the INI
 * file, and the task name is the section name. Inside a section, four entries configures the task:<br>
 * <li><code>class</code> is the fully qualified name of the class that implements the task.</li>
 * <li><code>description</code> is the description used when showing task information.</li>
 * <li><code>start</code> is the task start time in HH:MM:SS format.</li>
 * <li><code>stop</code> is the task stop time in HH:MM:SS format.</li><br>
 *
 * When the task start time equals the string <code>*</code> (the configurable daemon id string)
 * the task is then a daemon, and does not stop until the task itself ends.<br>
 *
 * The tasks are implemented extending the abstract class <code>SchedulerTask</code>.
 *
 * By default new tasks are loaded using the scheduler thread class loader, but it can
 * be configured to use any initialized class loader.<br>
 *
 * @author jorge.hidalgo
 * @version 2.5
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
     * Default value for <code>DATE_FORMAT</code> property.
     */
    private static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";

    /**
     * String that identifies a daemon task.
     */
    public static final String DAEMON_ID = "*";

    /**
     * The task class entry name.
     */
    private static final String TASK_CLASS_ENTRY_KEY = "class";

    /**
     * The task description entry name.
     */
    private static final String TASK_DESCRIPTION_ENTRY_KEY = "description";

    /**
     * The task start time entry name.
     */
    private static final String TASK_START_ENTRY_KEY = "start";

    /**
     * The task stop time entry name.
     */
    private static final String TASK_STOP_ENTRY_KEY = "stop";

    /**
     * The time token separator.
     */
    private static final String TIME_SEPARATOR = ":";

    /**
     * Scheduler thread sleep time.
     */
    private static final long SCHEDULER_SLEEP_TIME = 100;

    /**
     * The finalizer guardian.
     */
    final Object finalizerGuardian = new Object() {

        /**
         * Finalizes the object by stopping the current running tasks and the scheduler process.
         *
         * @throws java.lang.Throwable a throwable object
         *
         * @see java.lang.Object#finalize()
         */
        protected void finalize()
            // CHECKSTYLE:OFF
            throws java.lang.Throwable {
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
     * Constructor that sets the file that contains the tasks information.<br>
     *
     * A <code>java.lang.IllegalArgumentException</code> exception is thrown if a required key is
     * missing in the configuration file or a task class could not be successfully created or a task
     * start or stop time are not valid.
     *
     * @param iniFile the file with the tasks information
     *
     * @throws java.io.IOException an i/o exception
     */
    public Scheduler(File iniFile)
        throws java.io.IOException {

        this();

        // reads the configuration file
        INIFileManager ifm = new INIFileManager(iniFile);

        Iterator sections = ifm.getSections().iterator();
        while (sections.hasNext()) {
            String taskName = (String) sections.next();

            // the default section in the INI file is ignored
            if (taskName.length() == 0) {
                continue;
            }

            String taskClassName = ifm.getValue(taskName, TASK_CLASS_ENTRY_KEY);
            if (taskClassName == null) {
                throw new IllegalArgumentException("ERR_KEY_CLASS_NOT_FOUND");
            }

            String taskDescription = ifm.getValue(taskName, TASK_DESCRIPTION_ENTRY_KEY);
            if (taskDescription == null) {
                throw new IllegalArgumentException("ERR_KEY_DESCRIPTION_NOT_FOUND");
            }

            String tempStartTime = ifm.getValue(taskName, TASK_START_ENTRY_KEY);
            if (tempStartTime == null) {
                throw new IllegalArgumentException("ERR_KEY_START_NOT_FOUND");
            }

            Calendar taskStartTime = null;
            try {
                taskStartTime = parseTime(tempStartTime);
            } catch (IllegalArgumentException iae) {
                throw new IllegalArgumentException("ERR_TASK_INVALID_START_TIME", iae);
            }

            String tempStopTime = ifm.getValue(taskName, TASK_STOP_ENTRY_KEY);
            if (tempStopTime == null) {
                throw new IllegalArgumentException("ERR_KEY_STOP_NOT_FOUND");
            }

            Calendar taskStopTime = null;
            try {
                taskStopTime = parseTime(tempStopTime);
            } catch (IllegalArgumentException iae) {
                throw new IllegalArgumentException("ERR_TASK_INVALID_STOP_TIME", iae);
            }

            scheduleTask(taskName, taskClassName, taskDescription, taskStartTime, taskStopTime);
        }
    }
    /**
     * Constructor that sets the file that contains the tasks information using its name.<br>
     *
     * A <code>java.lang.IllegalArgumentException</code> exception is thrown if a required key is
     * missing in the configuration file or the task class could not be successfully created or a
     * task start or stop time are not valid.
     *
     * @param iniFileName the name of the file with the tasks information
     *
     * @throws java.io.IOException an i/o exception
     */
    public Scheduler(String iniFileName)
        throws java.io.IOException {

        this(new File(iniFileName));
    }

    /**
     * Parses a string containing a time in HH:MM:SS format. If the string equals the value in
     * <code>DAEMON_ID</code> the method returns <code>null</code>. The resulting
     * <code>java.util.Calendar</code> object date is the current date and its time is the parsed
     * time.<br>
     *
     * A <code>java.lang.IllegalArgumentException</code> exception is thrown if the input string
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

        try {
            if (timeString.equals(DAEMON_ID)) {
                return null;
            }

            java.util.StringTokenizer st = new java.util.StringTokenizer(timeString, TIME_SEPARATOR);

            Calendar retValue = Calendar.getInstance();

            retValue.set(Calendar.HOUR_OF_DAY, Integer.parseInt(st.nextToken()));
            retValue.set(Calendar.MINUTE, Integer.parseInt(st.nextToken()));
            retValue.set(Calendar.SECOND, Integer.parseInt(st.nextToken()));
            retValue.clear(Calendar.MILLISECOND);

            return retValue;
        } catch (NoSuchElementException nsee) {
            throw new IllegalArgumentException(nsee);
        } catch (NumberFormatException nfe) {
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
                    if (!task.isStarting() && !task.isExecuting() && !task.isDaemonExecuted()) {
                        task.taskStart();
                    }

                    continue;
                }

                if (task.getTaskNextStartTime() == null || task.getTaskNextStopTime() == null) {
                    task.setTaskNextStartTime(task.getTaskStartTime());

                    if (now.after(task.getTaskNextStartTime())) {
                        task.getTaskNextStartTime().add(Calendar.DAY_OF_MONTH, 1);
                    }

                    task.setTaskNextStopTime(task.getTaskStopTime());

                    if (task.getTaskNextStopTime().before(task.getTaskNextStartTime())) {
                        task.getTaskNextStopTime().add(Calendar.DAY_OF_MONTH, 1);
                    }
                }

                if (task.isExecuting()
                    && !task.isStopping()
                    && (now.equals(task.getTaskNextStopTime())
                        || now.after(task.getTaskNextStopTime()))) {
                    task.taskStop();
                }

                if (!task.isStarting()
                    && !task.isExecuting()
                    && (now.equals(task.getTaskNextStartTime())
                        || now.after(task.getTaskNextStartTime()))) {
                    task.taskStart();
                }
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
     * the method does nothing.<br>
     *
     * A <code>java.lang.IllegalArgumentException</code> exception is thrown if the task class
     * could not be successfully created.
     *
     * @param taskName the task name
     * @param taskClass the task class
     * @param taskDescription the task description
     * @param taskStartTime the task start time
     * @param taskStopTime the task stop time
     */
    public void scheduleTask(String taskName, Class taskClass, String taskDescription,
                             Calendar taskStartTime, Calendar taskStopTime) {

        synchronized (tasks) {
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

                    return;
                }
            }

            if (taskClass == null || taskDescription == null || taskDescription.length() == 0) {
                throw new IllegalArgumentException("ERR_TASK_INCOMPLETE");
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
                throw new IllegalArgumentException("ERR_TASK_CLASS_INVALID", nsme);
            } catch (InstantiationException ie) {
                throw new IllegalArgumentException("ERR_TASK_CLASS_INVALID", ie);
            } catch (IllegalAccessException iae) {
                throw new IllegalArgumentException("ERR_TASK_CLASS_INVALID", iae);
            } catch (java.lang.reflect.InvocationTargetException ite) {
                throw new IllegalArgumentException("ERR_TASK_CLASS_INVALID", ite);
            }
        }
    }

    /**
     * Schedules a new task or re-schedules an existing task. If the task exists and it is running,
     * the method does nothing. THe class is loaded using the shceduler class loader.<br>
     *
     * A <code>java.lang.IllegalArgumentException</code> exception is thrown if the task class
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
                throw new IllegalArgumentException("ERR_TASK_INCOMPLETE");
            }

            try {
                Class taskClass = Class.forName(taskClassName, true, schedulerClassLoader);

                scheduleTask(taskName, taskClass, taskDescription, taskStartTime, taskStopTime);

            } catch (ClassNotFoundException cnfe) {
                throw new IllegalArgumentException("ERR_TASK_NOT_FOUND", cnfe);
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
}
