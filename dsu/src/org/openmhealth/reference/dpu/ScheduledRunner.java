/*******************************************************************************
 * Copyright 2014 Open mHealth
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.openmhealth.reference.dpu;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
public class ScheduledRunner {
    private static final Logger LOGGER = 
        Logger.getLogger(ScheduledRunner.class.getName());

    @Scheduled(cron="0 0 4 * * *")
    public void run() {
        // Run all DPUs from the beginning of yesterday until 1 second before
        // the beginning of today.
        DateTime startOfToday = (new DateTime()).withTimeAtStartOfDay();
        DateTime startOfYesterday = startOfToday.plusDays(-1);
        DateTime endOfPeriod = startOfToday.plusSeconds(-1);

        String period =
            "all DPUs for the period of " + startOfYesterday.toString() 
            + " until " + endOfPeriod.toString() + ".";

        LOGGER.log(Level.INFO, "About to run " + period);
        Runner.run(startOfYesterday, endOfPeriod);
        LOGGER.log(Level.INFO, "Ran " + period);
    }
}
