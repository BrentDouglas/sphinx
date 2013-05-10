package io.machinecode.sphinx.browser.response;

import java.math.BigDecimal;

/**
 * @author Brent Douglas <brent.n.douglas@gmail.com>
 */
public class Status extends Base {
    private BigDecimal usedTime;
    private BigDecimal totalAvailableTime;
    private int runningWindowsSessions;
    private int windowsSessionsLimit;
    private int runningMacSessions;
    private int macSessionsLimit;

    public BigDecimal getUsedTime() {
        return usedTime;
    }

    public void setUsedTime(final BigDecimal usedTime) {
        this.usedTime = usedTime;
    }

    public BigDecimal getTotalAvailableTime() {
        return totalAvailableTime;
    }

    public void setTotalAvailableTime(final BigDecimal totalAvailableTime) {
        this.totalAvailableTime = totalAvailableTime;
    }

    public int getRunningWindowsSessions() {
        return runningWindowsSessions;
    }

    public void setRunningWindowsSessions(final int runningWindowsSessions) {
        this.runningWindowsSessions = runningWindowsSessions;
    }

    public int getWindowsSessionsLimit() {
        return windowsSessionsLimit;
    }

    public void setWindowsSessionsLimit(final int windowsSessionsLimit) {
        this.windowsSessionsLimit = windowsSessionsLimit;
    }

    public int getRunningMacSessions() {
        return runningMacSessions;
    }

    public void setRunningMacSessions(final int runningMacSessions) {
        this.runningMacSessions = runningMacSessions;
    }

    public int getMacSessionsLimit() {
        return macSessionsLimit;
    }

    public void setMacSessionsLimit(final int macSessionsLimit) {
        this.macSessionsLimit = macSessionsLimit;
    }
}
