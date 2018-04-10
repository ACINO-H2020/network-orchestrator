/*
 * Copyright (c) 2018 ACINO Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onosproject.orchestrator.dismi.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


/**
 * Created by aghafoor on 2017-09-07.
 */
public class DismiPerformanceTest {
    public boolean active = true;
    private File file = null;

    public DismiPerformanceTest(String path) {
        if (!(path.endsWith("/") || path.endsWith("\\"))) {
            path = path + File.separator;
        }
        file = new File(path + "dismiper.txt");

    }

    public void write(String entry) {
        if (!active) {
            return;
        }

        BufferedWriter bw = null;
        FileWriter fw = null;
        try {

            if (!file.exists()) {
                file.createNewFile();
            }

            // true = append file
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            bw.write(entry + ":" + System.currentTimeMillis() + "\n");
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } finally {
            try {

                if (bw != null) {
                    bw.close();
                }

                if (fw != null) {
                    fw.close();
                }

            } catch (IOException ex) {

                ex.printStackTrace();

            }
        }
    }

    public void process() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                String everything = sb.toString();
                System.out.println(everything);
            } finally {
                br.close();
            }
        } catch (Exception exp) {

        }
    }

    public String filePath() {
        return file.getAbsolutePath();
    }

    public boolean setActive(boolean active) {
        return this.active = active;
    }

    public boolean isActive() {
        return active;
    }
}
