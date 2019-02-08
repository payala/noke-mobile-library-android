package com.noke.nokemobilelibrary;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/************************************************************************************************************************************************
 * Copyright © 2018 Nokē Inc. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by Spencer Apsley on 1/17/18.
 * Class stores information about the Noke device and contains methods for interacting with the Noke device
 */

public class NokeDevice {

    /**
     * Time Interval of the most recent time the device was discovered
     */
    private long lastSeen;

    /**
     * Name of the Noke device (strictly cosmetic)
     */
    private String name;
    /**
     * MAC address of the Noke device. This can be found in the peripheral name
     */
    private String mac;
    /**
     * Serial number of Noke device. Laser engraved onto the device during manufacturing
     */
    private String serial;
    /**
     * Firmware and hardware version of the lock. Follows format: '3P-2.10' where '3P' is the hardware version and '2.10' is the firmware version
     */
    private String version;
    /**
     * Tracking key used to track Noke device usage and activity
     */
    private String trackingKey;
    /**
     * BluetoothDevice used for interacting with the Noke device via bluetooth
     */
    BluetoothDevice bluetoothDevice;
    /**
     * Provides Bluetooth GATT functionality to enable communication with Bluetooth Smart devices
     */
    transient BluetoothGatt gatt;
    /**
     * 40 char string read from the session characteristic upon connecting to the Noke device
     */
    transient String session;
    /**
     * Battery level of the Noke device in millivolts
     */
    private Integer battery;
    /**
     * Unlock command used with the offline key to unlock the lock without a network connection
     */
    private String offlineUnlockCmd;
    /**
     * Offline key generated by the API that can be cached on the phone to unlock the lock without a network connection
     */
    private String offlineKey;
    /**
     * Connection state of the Noke device
     */
    transient int connectionState;
    /**
     * Lock state of the Noke device
     */
    transient int lockState;
    /**
     * Attempts made to connect. Sometimes if the gatt throws an error, simply retrying will work. We make 4 attempts to connect before throwing an error
     */
    transient int connectionAttempts;
    /**
     * Single strength of the Noke device broadcast
     */
    transient int rssi;
    /**
     * Array of commands to be sent to the Noke device
     */
    transient ArrayList<String> commands;
    /**
     * Reference to the bluetooth service that manages to the device
     */
    transient NokeDeviceManagerService mService;
    /**
     * Boolean that indicates if the lock is being restored by the Core API
     */
    transient boolean isRestoring;


    /**
     * Initializes Noke Device
     *
     * @param name name of the device
     * @param mac  MAC address of the device
     */
    public NokeDevice(String name, String mac) {

        this.name = name;
        this.mac = mac;

        commands = new ArrayList<>();
        connectionAttempts = 0;
    }

    /**
     * Parses through the broadcast data and pulls out the version
     *
     * @param broadcastBytes broadcast data from the lock
     * @param deviceName     bluetooth device name of the lock. This contains the hardware version
     * @return version string. @see #version
     */
    @SuppressWarnings("WeakerAccess")
    public String getVersion(byte[] broadcastBytes, String deviceName) {
        String version = "";
        if (deviceName != null) {
            if (deviceName.contains(NokeDefines.NOKE_DEVICE_IDENTIFER_STRING)) {
                if (broadcastBytes != null && broadcastBytes.length > 0) {
                    int majorVersion = broadcastBytes[1];
                    int minorVersion = broadcastBytes[2];
                    String hardwareVersion = deviceName.substring(4, 6);
                    return hardwareVersion + "-" + majorVersion + "." + minorVersion;
                } else {
                    if (deviceName.contains("2P")) {
                        return "P2.0";
                    } else {
                        if (deviceName.contains("FOB")) {
                            return "1F-1.0";
                        } else {
                            return "1P-1.0";
                        }
                    }
                }
            } else {
                return "NOT A NOKE DEVICE";
            }
        }
        return version;
    }

    /**
     * Sets the session of the lock after connecting.  Session is only valid for the duration that the lock is connected
     *
     * @param sessionIn 20 byte array of the session read from the session characteristic
     */
    void setSession(byte[] sessionIn) {
        byte[] batteryArray = new byte[]{sessionIn[3], sessionIn[2]};
        String batteryString = NokeDefines.bytesToHex(batteryArray);
        battery = Integer.parseInt(batteryString, 16);
        if (sessionIn.length >= 20) {
            session = NokeDefines.bytesToHex(sessionIn);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @SuppressWarnings("WeakerAccess")
    public String getMac() {
        return mac;
    }

    @SuppressWarnings("unused")
    public void setMac(String mac) {
        this.mac = mac;
    }

    @SuppressWarnings("unused")
    public String getSerial() {
        return serial;
    }

    @SuppressWarnings("unused")
    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @SuppressWarnings("unused")
    public String getOfflineUnlockCmd() {
        return offlineUnlockCmd;
    }

    @SuppressWarnings("unused")
    public void setOfflineUnlockCmd(String offlineUnlockCmd) {
        this.offlineUnlockCmd = offlineUnlockCmd;
    }

    @SuppressWarnings("unused")
    public String getOfflineKey() {
        return offlineKey;
    }

    @SuppressWarnings("unused")
    public void setOfflineKey(String offlineKey) {
        this.offlineKey = offlineKey;
    }

    @SuppressWarnings("unused")
    public long getLastSeen() {
        return lastSeen;
    }

    void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    @SuppressWarnings("unused")
    public String getSession() {
        return session;
    }

    @SuppressWarnings("unused")
    public void setSession(String session) {
        this.session = session;
    }

    @SuppressWarnings("unused")
    public Integer getBattery() {
        return battery;
    }

    @SuppressWarnings("unused")
    public void setBattery(Integer battery) {
        this.battery = battery;
    }

    @SuppressWarnings("unused")
    public String getTrackingKey() {
        return trackingKey;
    }

    @SuppressWarnings("unused")
    public void setTrackingKey(String trackingKey) {
        this.trackingKey = trackingKey;
    }

    @SuppressWarnings("unused")
    public int getConnectionState() {
        return connectionState;
    }

    @SuppressWarnings("unused")
    public void setConnectionState(int connectionState) {
        this.connectionState = connectionState;
    }

    /**
     * Sends a + delimited string of commands to the lock
     *
     * @param commands + delimited string returned from the unlock request
     */
    public void sendCommands(String commands) {
        List<String> commandArr = Arrays.asList(commands.split("\\+"));
        this.commands.addAll(commandArr);
        if (this.commands.size() > 1) {
            this.connectionState = NokeDefines.NOKE_STATE_SYNCING;
            mService.getNokeListener().onNokeSyncing(this);
        }
        mService.writeRXCharacteristic(this);
    }

    /**
     * Sends an arrayList of commands to the lock
     *
     * @param commands an arrayList of strings (NOT USED WITH THE CORE API)
     */
    public void sendCommands(ArrayList<String> commands){
        this.commands = commands;
        if (this.commands.size() > 1) {
            this.connectionState = NokeDefines.NOKE_STATE_SYNCING;
            mService.getNokeListener().onNokeSyncing(this);
        }
        mService.writeRXCharacteristic(this);
    }

    /**
     * Checks for a valid offline key and offline unlock and unlocks the lock without a network connection
     */
    public void offlineUnlock() {
        if (this.offlineUnlockCmd.length() == NokeDefines.UNLOCK_COMMAND_LENGTH && this.offlineKey.length() == NokeDefines.OFFLINE_KEY_LENGTH) {
            byte unlockCommand[] = NokeDefines.hexToBytes(this.offlineUnlockCmd);

            byte header[] = new byte[4];
            header[0] = unlockCommand[0];
            header[1] = unlockCommand[1];
            header[2] = unlockCommand[2];
            header[3] = unlockCommand[3];

            byte commandpacket[] = new byte[20];
            System.arraycopy(header, 0, commandpacket, 0, header.length);

            byte cmddata[] = new byte[16];
            System.arraycopy(unlockCommand, 4, cmddata, 0, 16);

            long unixTime = System.currentTimeMillis() / 1000L;

            ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE);
            buffer.putLong(unixTime);
            byte timestamp[] = buffer.array();

            cmddata[2] = timestamp[7];
            cmddata[3] = timestamp[6];
            cmddata[4] = timestamp[5];
            cmddata[5] = timestamp[4];

            byte preSessionKey[] = NokeDefines.hexToBytes(this.offlineKey);
            byte sessionBytes[] = NokeDefines.hexToBytes(this.session);

            for (int x = 0; x < preSessionKey.length; x++) {
                int total = NokeDefines.toUnsigned(preSessionKey[x]) + NokeDefines.toUnsigned(sessionBytes[x]);
                preSessionKey[x] = (byte) total;
            }

            byte checksum = 0;
            for (int x = 0; x < 15; x++) {
                checksum += cmddata[x];
            }

            cmddata[15] = checksum;
            System.arraycopy(encryptPacket(preSessionKey, cmddata), 0, commandpacket, 4, 16);

            this.commands.add(NokeDefines.bytesToHex(commandpacket));
            mService.writeRXCharacteristic(this);
        } else {
            mService.getNokeListener().onError(this, NokeMobileError.ERROR_INVALID_OFFLINE_KEY, "Offline key/command is invalid.");
        }
    }

    /**
     * Used to encrypt command packets going to the lock
     *
     * @param combinedkey key for encrypting the commands
     * @param data        data to be encrypted
     * @return returns encrypted data packet
     */
    private byte[] encryptPacket(byte combinedkey[], byte data[]) {
        byte buffer[] = new byte[16];
        byte tmpkey[] = new byte[16];
        System.arraycopy(combinedkey, 0, tmpkey, 0, 16);

        AesLibrary.aes_enc_dec(data, tmpkey, (byte) 1);
        System.arraycopy(data, 0, buffer, 0, 16);
        return buffer;
    }

    public int getLockState() {
        return lockState;
    }

    public void setLockState(int lockState) {
        this.lockState = lockState;
    }

    public String getHardwareVersion(){
        return this.version.substring(0,2);
    }

    public String getSoftwareVersion(){
        return this.version.substring(3);
    }

}
