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

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.openmhealth.reference.exception.OmhException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ManualRunner {
    public static void main(String[] args) {
        if (args.length != 2) {
            throw new RuntimeException(
                "Usage: java -jar dpu.jar <startDateTime> <endDateTime>");
        }

        DateTime startDate = null;
        DateTime endDate = null;
        try {
            DateTimeFormatter formatter = 
                ISODateTimeFormat.dateOptionalTimeParser();
            startDate = formatter.parseDateTime(args[0]);
            endDate = formatter.parseDateTime(args[1]);
        } catch (Exception e) {
            throw new OmhException("Error parsing dates", e);
        }

        ClassPathXmlApplicationContext context =
            new ClassPathXmlApplicationContext(
                "/config/spring/payload.xml", ManualRunner.class);

        try {
            Runner.run(startDate, endDate);
        } finally {
            context.close();
        }
    }
}
