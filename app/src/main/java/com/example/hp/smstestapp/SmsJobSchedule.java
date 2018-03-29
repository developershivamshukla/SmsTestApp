package com.example.hp.smstestapp;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

public class SmsJobSchedule extends JobService {
    @Override
    public boolean onStartJob(JobParameters job) {

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }
}
