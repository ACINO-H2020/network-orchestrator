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

package org.onosproject.orchestrator.dismi.primitives.extended;

import org.onosproject.orchestrator.dismi.utils.InputAssertion;

/**
 * Created by stephane on 10/13/16.
 */
public class IPAddress {
    private IP ip = new IP();
    private IP mask = new IP();

    public IPAddress() {

    }

    public IPAddress(String addr) {
        ip.setAddress(addr);
    }

    public IPAddress(String addr, String m) {
        ip.setAddress(addr);
        mask.setAddress(m);
    }

    public boolean isAddressValid() {
        return ip.isValidAddress();
    }

    public boolean isMaskValid() {
        return mask.isValidMask();
    }

    public boolean setAddress(String addr) {
        return ip.setAddress(addr);
    }

    public boolean setMask(String m) {
        return mask.setAddress(m);
    }

    public boolean setAddressAndMask(String addr) {

        // This means that it is not a valid ip/mask.
        if (!(addr.contains("/"))) {
            return false;
        }

        String[] list = addr.split("/");
        int i;
        String str;

        if (list.length != 2) {
            return false;
        }

        String address = list[0];
        String m = list[1];

        if (ip.setAddress(address) && mask.setAddress(m)) {
            return true;
        }

        return false;
    }

    private class IP {
        private int[] addr = null;


        public IP() {
            addr = new int[4];
            for (int i = 0; i < 4; i++) {
                addr[i] = -1;
            }
        }

        boolean isValidAddress() {
            return isValid(addr[0]) &&
                    isValid(addr[1]) &&
                    isValid(addr[2]) &&
                    isValid(addr[3]);
        }

        boolean isValidMask() {
            int m = getMaskInt();
            // The mask is invalid.
            if (m < 0) {
                return false;
            }
            return true;
        }

        int getMaskInt() {
            int exp;
            int i, j;
            int mask = 0;
            int a, b;
            int invalid = -1;

            for (i = 0; i < 4; i++) {
                a = addr[i];
                exp = 8;

                for (exp = 7; exp >= 0; exp--) {
                    b = a - (2 ^ exp);
                    if (b > 0) {
                        a = b;
                        mask++;
                    } else if (b < 0) { // This mask is not a power of 2!
                        return invalid;
                    } else if (exp == 0) {    // b == 0: if exp>0, then it should be the end of the mask
                        for (j = i + 1; j < 4; j++) {
                            if (addr[j] != 0) {   // this shold be zero!
                                return invalid;
                            }
                        }
                        return mask;
                    }
                }
            }

            return mask;    // If we are here, the mask is 255.255.255.255 == 32
        }

        boolean isValid(int i) {
            if ((i >= 0) && (i <= 255)) {
                return true;
            }
            return false;

        }

        public boolean setA(int i) {
            if (isValid(i)) {
                addr[0] = i;
                return true;
            }
            return false;
        }

        public boolean setB(int i) {
            if (isValid(i)) {
                addr[1] = i;
                return true;
            }
            return false;
        }

        public boolean setC(int i) {
            if (isValid(i)) {
                addr[2] = i;
                return true;
            }
            return false;
        }

        public boolean setD(int i) {
            if (isValid(i)) {
                addr[3] = i;
                return true;
            }
            return false;
        }

        /*
            TODO: Not implemented yet
         */
        public boolean setAddressFromInt(int m) {
            if ((m > 32) || (m < 0)) {   // Not a mask after all
                return false;
            }

            return true;
        }

        public boolean setAddress(String ip) {
            InputAssertion inputAssertion = new InputAssertion();
            if (null == ip) {
                return false;
            }
            String[] address = ip.split("\\.");
            int i;
            String str;

            if (address.length == 4) {
                int[] abcd = new int[4];
                for (i = 0; i < 4; i++) {
                    str = address[i];
                    try {
                        abcd[i] = Integer.valueOf(str);
                        if ((abcd[i] < 0) || (abcd[i] > 255)) { // Invalid
                            return false;
                        }
                    } catch (Exception e) {
                        return false;
                    }
                }
                for (i = 0; i < 4; i++) {
                    addr[i] = abcd[i];
                }
                return true;
            } else if (address.length == 1) {   // Formatted as a mask!
                int m;
                try {
                    m = Integer.valueOf(ip);
                } catch (Exception e) {
                    return false;
                }
                return setAddressFromInt(m);
            }
            return false;
        }
    }
}
