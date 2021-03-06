/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bluepandora.therap.donatelife.gcmservice;

import com.bluepandora.therap.donatelife.constant.DbUser;
import com.bluepandora.therap.donatelife.database.DatabaseService;
import com.bluepandora.therap.donatelife.database.GetQuery;
import com.bluepandora.therap.donatelife.debug.Debug;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;

import com.google.android.gcm.server.Sender;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Biswajit Debnath
 * These Class give the  GOOGLE cloud messaging service
 */
public class GcmService {

    private static final String GOOGLE_SERVER_KEY = "AIzaSyA_1Xk1GFUu_T6bth1erowm4hD6nTCAoFw";
    private static final String MESSAGE_KEY = "message";


    public static void giveGCMService(HttpServletRequest request, HttpServletResponse response, DatabaseService dbService) {

        if (request.getParameter("groupId") != null && request.getParameter("hospitalId") != null && request.getParameter("mobileNumber") != null) {
            String groupId = request.getParameter("groupId");
            String hospitalId = request.getParameter("hospitalId");
            String mobileNumber = request.getParameter("mobileNumber");

            String donatorMessage = getMessage(groupId, hospitalId, dbService);
            Debug.debugLog("Donator Message:", donatorMessage);
            List gcmIDList = FindDonator.findDonatorGCMId(groupId, hospitalId, dbService);
            Debug.debugLog(gcmIDList);

            int donatorCount = gcmIDList.size();

            if (donatorCount != 0) {
                sendNotificationToDonator(request, response, gcmIDList, donatorMessage);
            }

            gcmIDList = FindDonator.findDonatorGCMId(mobileNumber, dbService);

            if (gcmIDList.size() != 0) {

                if (donatorCount <= 1) {
                    sendNotificationToDonator(request, response, gcmIDList, "NOTIFIED " + donatorCount + " PERSON");
                } else {
                    sendNotificationToDonator(request, response, gcmIDList, "NOTIFIED " + donatorCount + " PERSONS");
                }

            }
        }
    }

    /**
     * 
     * @param groupId
     * @param hospitalId
     * @param dbService
     * @return This method make the message which will be send to donator
     */
    private static String getMessage(String groupId, String hospitalId, DatabaseService dbService) {
        String query = GetQuery.getBloodGroupNameQuery(groupId);
        ResultSet result = dbService.getResultSet(query);

        String groupName = null;
        String hospitalName = null;
        String message = null;

        try {

            while (result.next()) {
                groupName = result.getString("group_name");
            }
            query = GetQuery.getHospitalNameQuery(hospitalId);

            result = dbService.getResultSet(query);

            while (result.next()) {
                hospitalName = result.getString("hospital_name");
            }

        } catch (SQLException error) {
            Debug.debugLog("BLOOD GROUP NAME SQL ERROR", error);
        }

        if (groupName != null && hospitalName != null) {
            message = "NEED " + groupName + " IN " + hospitalName;
        }

        return message;
    }

    /**
     * 
     * @param request
     * @param response
     * @param donatorList
     * @param donatorMessage 
     * This method send the notification message to the donator
     */
    private static void sendNotificationToDonator(HttpServletRequest request, HttpServletResponse response, List donatorList, String donatorMessage) {

        try {
            Sender sender = new Sender(GOOGLE_SERVER_KEY);
            Message message = new Message.Builder()
                    .delayWhileIdle(true).addData(MESSAGE_KEY, donatorMessage)
                    .build();
            sender.send(message, donatorList, 1);
        } catch (IOException ioe) {
            Debug.debugLog("SENDING NOTIFICATION IO EXCEPTION!");
        } catch (Exception error) {
            Debug.debugLog("SENDING NOTIFICATION EXCEPTION!");
        }
    }
}
