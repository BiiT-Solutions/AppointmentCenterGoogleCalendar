package com.biit.appointment.google;

import com.biit.appointment.google.logger.TestLogging;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        TestLogging.info(this.getClass(), "### Test started '" + result.getMethod().getMethodName() + "' from '" + result.getTestClass().getName() + "'.");
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        TestLogging.info(this.getClass(), "### Test finished '" + result.getMethod().getMethodName() + "' from '" + result.getTestClass().getName() + "'.");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        TestLogging.errorMessage(this.getClass(), "### Test failed '" + result.getMethod().getMethodName() + "' from '" + result.getTestClass().getName() + "'.");
    }

    @Override
    public void onTestSkipped(ITestResult result) {

    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {

    }

    @Override
    public void onStart(ITestContext context) {
        TestLogging.info(this.getClass(), "##### Starting tests from '" + context.getName() + "'.");
    }

    @Override
    public void onFinish(ITestContext context) {
        TestLogging.info(this.getClass(), "##### Tests finished from '" + context.getName() + "'.");
    }
}
