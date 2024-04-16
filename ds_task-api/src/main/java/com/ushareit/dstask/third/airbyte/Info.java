package com.ushareit.dstask.third.airbyte;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * @author fengxiao
 * @date 2022/6/21
 */
public interface Info {
    /**
     * Returns the executable pathname of the process.
     *
     * @return an {@code Optional<String>} of the executable pathname
     *         of the process
     */
    public Optional<String> command();

    /**
     * Returns the command line of the process.
     * <p>
     * If {@link #command command()} and  {@link #arguments arguments()} return
     * non-empty optionals, this is simply a convenience method which concatenates
     * the values of the two functions separated by spaces. Otherwise it will return a
     * best-effort, platform dependent representation of the command line.
     *
     * @apiNote Note that the returned executable pathname and the
     *          arguments may be truncated on some platforms due to system
     *          limitations.
     *          <p>
     *          The executable pathname may contain only the
     *          name of the executable without the full path information.
     *          It is undecideable whether white space separates different
     *          arguments or is part of a single argument.
     *
     * @return an {@code Optional<String>} of the command line
     *         of the process
     */
    public Optional<String> commandLine();

    /**
     * Returns an array of Strings of the arguments of the process.
     *
     * @apiNote On some platforms, native applications are free to change
     *          the arguments array after startup and this method may only
     *          show the changed values.
     *
     * @return an {@code Optional<String[]>} of the arguments of the process
     */
    public Optional<String[]> arguments();

    /**
     * Returns the start time of the process.
     *
     * @return an {@code Optional<Instant>} of the start time of the process
     */
    public Optional<Instant> startInstant();

    /**
     * Returns the total cputime accumulated of the process.
     *
     * @return an {@code Optional<Duration>} for the accumulated total cputime
     */
    public Optional<Duration> totalCpuDuration();

    /**
     * Return the user of the process.
     *
     * @return an {@code Optional<String>} for the user of the process
     */
    public Optional<String> user();
}
