package deors.demos.testing.mocks.servletmocks;

import java.util.Calendar;

/**
 * Abstract class that represents a scheduler task.<br>
 *
 * Classes that extends <code>SchedulerTask</code> must implement the
 * <code>taskPrepareStart()</code>, <code>taskPrepareStop()</code> and <code>taskLogic()</code>
 * methods.<br>
 *
 * @author jorge.hidalgo
 * @version 2.4
 */
public abstract class SchedulerTask
    implements Runnable {

    /**
     * The task execution flag.
     *
     * @see SchedulerTask#isExecuting()
     */
    private boolean executing;

    /**
     * The task starting flag.
     *
     * @see SchedulerTask#isStarting()
     */
    private boolean starting;

    /**
     * The task stopping flag.
     *
     * @see SchedulerTask#isStopping()
     */
    private boolean stopping;

    /**
     * The task name.
     *
     * @see SchedulerTask#getTaskName()
     * @see SchedulerTask#setTaskName(String)
     */
    private String taskName;

    /**
     * The task description.
     *
     * @see SchedulerTask#getTaskDescription()
     * @see SchedulerTask#setTaskDescription(String)
     */
    private String taskDescription;

    /**
     * The task start time.
     *
     * @see SchedulerTask#getTaskStartTime()
     * @see SchedulerTask#setTaskStartTime(Calendar)
     */
    private Calendar taskStartTime;

    /**
     * The task stop time.
     *
     * @see SchedulerTask#getTaskStopTime()
     * @see SchedulerTask#setTaskStopTime(Calendar)
     */
    private Calendar taskStopTime;

    /**
     * The task next start time.
     *
     * @see SchedulerTask#getTaskNextStartTime()
     * @see SchedulerTask#setTaskNextStartTime(Calendar)
     */
    private Calendar taskNextStartTime;

    /**
     * The task next stop time.
     *
     * @see SchedulerTask#getTaskNextStopTime()
     * @see SchedulerTask#setTaskNextStopTime(Calendar)
     */
    private Calendar taskNextStopTime;

    /**
     * The task thread.
     */
    @SuppressWarnings("PMD.AvoidUsingVolatile")
    volatile Thread taskThread;

    /**
     * The task thread saved for killing it if needed (e.g. the thread
     * is not responding to the stop signal).
     */
    @SuppressWarnings("PMD.AvoidUsingVolatile")
    volatile Thread taskThread4Kill;

    /**
     * This flag indicates whether this task has been scheduled as a daemon (used by the
     * <code>Scheduler</code> class).
     */
    private boolean daemonTask;

    /**
     * This flag indicates whether this task (if a daemon) has been already started (used by the
     * <code>Scheduler</code> class so a daemon does not start again after its first execution).
     */
    private boolean daemonExecuted;

    /**
     * Text used in the <code>toString()</code> method to surround the task description.
     */
    private static final String TASK_DESCRIPTION_END = ")";

    /**
     * Text used in the <code>toString()</code> method to surround the task description.
     */
    private static final String TASK_DESCRIPTION_START = " (";

    /**
     * The finalizer guardian.
     */
    final Object finalizerGuardian = new Object() {

        /**
         * Finalizes the object by stopping the task.
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
                if (isExecuting()) {
                    taskAutoStop();
                }
            } finally {
                super.finalize();
            }
        }
    };

    /**
     * Task constructor.
     *
     * @param taskName the task name
     * @param taskDescription the task description
     * @param taskStartTime the task start time
     * @param taskStopTime the task stop time
     */
    public SchedulerTask(String taskName, String taskDescription, Calendar taskStartTime,
                         Calendar taskStopTime) {
        super();
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.taskStartTime = taskStartTime;
        this.taskStopTime = taskStopTime;
        this.taskThread = Thread.currentThread();
    }

    /**
     * Returns the <code>taskDescription</code> property value.
     *
     * @return the property value
     *
     * @see SchedulerTask#taskDescription
     * @see SchedulerTask#setTaskDescription(String)
     */
    public String getTaskDescription() {
        return taskDescription;
    }

    /**
     * Returns the <code>taskName</code> property value.
     *
     * @return the property value
     *
     * @see SchedulerTask#taskName
     * @see SchedulerTask#setTaskName(String)
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * Returns the <code>taskNextStartTime</code> property value.
     *
     * @return the property value
     *
     * @see SchedulerTask#taskNextStartTime
     * @see SchedulerTask#setTaskNextStartTime(Calendar)
     */
    public Calendar getTaskNextStartTime() {
        return taskNextStartTime;
    }

    /**
     * Returns the <code>taskNextStopTime</code> property value.
     *
     * @return the property value
     *
     * @see SchedulerTask#taskNextStopTime
     * @see SchedulerTask#setTaskNextStopTime(Calendar)
     */
    public Calendar getTaskNextStopTime() {
        return taskNextStopTime;
    }

    /**
     * Returns the <code>taskStartTime</code> property value.
     *
     * @return the property value
     *
     * @see SchedulerTask#taskStartTime
     * @see SchedulerTask#setTaskStartTime(Calendar)
     */
    public Calendar getTaskStartTime() {
        return taskStartTime;
    }

    /**
     * Returns the <code>taskStopTime</code> property value.
     *
     * @return the property value
     *
     * @see SchedulerTask#taskStopTime
     * @see SchedulerTask#setTaskStopTime(Calendar)
     */
    public Calendar getTaskStopTime() {
        return taskStopTime;
    }

    /**
     * Returns the <code>daemonExecuted</code> property value.
     *
     * @return the property value
     *
     * @see SchedulerTask#daemonExecuted
     * @see SchedulerTask#setDaemonExecuted(boolean)
     */
    public boolean isDaemonExecuted() {
        return daemonExecuted;
    }

    /**
     * Returns the <code>daemonTask</code> property value.
     *
     * @return the property value
     *
     * @see SchedulerTask#daemonTask
     * @see SchedulerTask#setDaemonTask(boolean)
     */
    public boolean isDaemonTask() {
        return daemonTask;
    }

    /**
     * Returns the <code>executing</code> property value.
     *
     * @return the property value
     *
     * @see SchedulerTask#executing
     */
    public boolean isExecuting() {
        return executing;
    }

    /**
     * Returns the <code>starting</code> property value.
     *
     * @return the property value
     *
     * @see SchedulerTask#starting
     */
    public boolean isStarting() {
        return starting;
    }

    /**
     * Returns the <code>stopping</code> property value.
     *
     * @return the property value
     *
     * @see SchedulerTask#stopping
     */
    public boolean isStopping() {
        return stopping;
    }

    /**
     * Executes the task logic.
     *
     * @see SchedulerTask#taskLogic()
     */
    @SuppressWarnings({
        "PMD.CompareObjectsWithEquals",
        "PMD.AvoidCatchingThrowable"
    })
    public void run() {

        Thread thisThread = Thread.currentThread();

        try {
            while (thisThread == taskThread) {
                taskLogic();
            }
        } catch (ThreadDeath td) {
            taskThread = null;

            throw td;

        // CHECKSTYLE:OFF
        } catch (Throwable t) {
        // CHECKSTYLE:ON

            taskThread = null;
        }

        if (taskThread == null) {
            taskAutoStop();
        }
    }

    /**
     * Sets the <code>daemonExecuted</code> property value.
     *
     * @param daemonExecuted the property new value
     *
     * @see SchedulerTask#daemonExecuted
     * @see SchedulerTask#isDaemonExecuted()
     */
    void setDaemonExecuted(boolean daemonExecuted) {
        this.daemonExecuted = daemonExecuted;
    }

    /**
     * Sets the <code>daemonTask</code> property value.
     *
     * @param daemonTask the property new value
     *
     * @see SchedulerTask#daemonTask
     * @see SchedulerTask#isDaemonTask()
     */
    void setDaemonTask(boolean daemonTask) {
        this.daemonTask = daemonTask;
    }

    /**
     * Sets the <code>taskDescription</code> property value.
     *
     * @param taskDescription the property new value
     *
     * @see SchedulerTask#taskDescription
     * @see SchedulerTask#getTaskDescription()
     */
    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    /**
     * Sets the <code>taskName</code> property value.
     *
     * @param taskName the property new value
     *
     * @see SchedulerTask#taskName
     * @see SchedulerTask#getTaskName()
     */
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    /**
     * Sets the <code>taskNextStartTime</code> property value.
     *
     * @param taskNextStartTime the property new value
     *
     * @see SchedulerTask#taskNextStartTime
     * @see SchedulerTask#getTaskNextStartTime()
     */
    public void setTaskNextStartTime(Calendar taskNextStartTime) {
        this.taskNextStartTime = taskNextStartTime;
    }

    /**
     * Sets the <code>taskNextStopTime</code> property value.
     *
     * @param taskNextStopTime the property new value
     *
     * @see SchedulerTask#taskNextStopTime
     * @see SchedulerTask#getTaskNextStopTime()
     */
    public void setTaskNextStopTime(Calendar taskNextStopTime) {
        this.taskNextStopTime = taskNextStopTime;
    }

    /**
     * Sets the <code>taskStartTime</code> property value.
     *
     * @param taskStartTime the property new value
     *
     * @see SchedulerTask#taskStartTime
     * @see SchedulerTask#getTaskStartTime()
     */
    public void setTaskStartTime(Calendar taskStartTime) {
        this.taskStartTime = taskStartTime;
    }

    /**
     * Sets the <code>taskStopTime</code> property value.
     *
     * @param taskStopTime the property new value
     *
     * @see SchedulerTask#taskStopTime
     * @see SchedulerTask#getTaskStopTime()
     */
    public void setTaskStopTime(Calendar taskStopTime) {
        this.taskStopTime = taskStopTime;
    }

    /**
     * The task itself stops the execution thread.
     *
     * @see SchedulerTask#taskPrepareStop()
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    protected void taskAutoStop() {

        if (executing && !stopping) {
            taskThread = null;

            setTaskNextStartTime(null);
            setTaskNextStopTime(null);

            try {
                stopping = true;
                taskPrepareStop();
                stopping = false;

                executing = false;

                if (daemonTask) {
                    daemonExecuted = true;
                }
            // CHECKSTYLE:OFF
            } catch (Throwable t) {
            // CHECKSTYLE:ON
            }
        }
    }

    /**
     * Kills the task. This method uses the deprecated <code>java.lang.Thread.stop()</code> method
     * to stop the running thread and invokes the task <code>taskAutoStop()</code> method to try
     * to perform the task stop logic.
     *
     * @see java.lang.Thread#stop()
     * @see SchedulerTask#taskAutoStop()
     */
    @SuppressWarnings("deprecation")
    void taskKill() {

        if (taskThread != null) {
            taskThread.stop();
        }
        if (taskThread4Kill != null) {
            taskThread4Kill.stop();
        }
        taskAutoStop();
    }

    /**
     * The task logic.
     */
    protected abstract void taskLogic();

    /**
     * Prepares the task start.
     *
     * @throws java.lang.Throwable a <code>Throwable</code> object
     */
    protected abstract void taskPrepareStart()
        // CHECKSTYLE:OFF
        throws java.lang.Throwable;
        // CHECKSTYLE:ON

    /**
     * Prepares the task stop.
     *
     * @throws java.lang.Throwable a <code>Throwable</code> object
     */
    protected abstract void taskPrepareStop()
        // CHECKSTYLE:OFF
        throws java.lang.Throwable;
        // CHECKSTYLE:ON

    /**
     * Starts the task thread execution. This method is called by the scheduler.
     *
     * @see SchedulerTask#taskPrepareStart()
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    void taskStart() {

        if (!starting && !executing && !stopping) {
            try {
                daemonExecuted = false;

                starting = true;
                taskPrepareStart();
                starting = false;

                executing = true;

                taskThread = new Thread(this);
                taskThread.start();

            // CHECKSTYLE:OFF
            } catch (Throwable t) {
            // CHECKSTYLE:ON

                taskThread = null;

                executing = false;

                setTaskNextStartTime(null);
                setTaskNextStopTime(null);
            }
        }
    }

    /**
     * Stops the task thread execution. This method is called by the scheduler.
     *
     * @see SchedulerTask#taskPrepareStop()
     */
    void taskStop() {

        if (taskThread != null) {
            taskThread4Kill = taskThread;
            taskThread = null;
        }
    }

    /**
     * Returns the task name and description.
     *
     * @return the task information
     */
    public String toString() {

        return taskName + TASK_DESCRIPTION_START + taskDescription + TASK_DESCRIPTION_END;
    }
}
