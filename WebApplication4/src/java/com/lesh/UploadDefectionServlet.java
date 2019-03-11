/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lesh;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Александр
 */
@MultipartConfig
public class UploadDefectionServlet extends HttpServlet {

    private static final String SAVE_DIR = "uploadFiles";

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ArrayList<Part> parts = (ArrayList<Part>) request.getParts();

        InputStream in = parts.get(0).getInputStream();

        byte[] buf = new byte[in.available()];
        int n = 0;
        while ((n = in.read(buf)) != -1) {
        }
        in.close();
        String JSON = new String(buf, "UTF-8");
        String driver = "com.mysql.jdbc.Driver";
        String connection = "jdbc:mysql://127.0.0.1:3306/leshenko?useSSL=false";
        String user = "root";
        String password = "11111";
        try {
            Class.forName(driver);
            try (Connection con = DriverManager.getConnection(connection, user, password)) {
                JSONArray jsonArrayDefections;
                if (!JSON.equals("")) {
                    try {
                        jsonArrayDefections = new JSONArray(JSON);
                        for (int i = 0; i < jsonArrayDefections.length(); i++) {
                            JSONObject obj = jsonArrayDefections.getJSONObject(i);
                            JSONObject point = obj.getJSONObject("coordinates");
                            PreparedStatement st = con.prepareStatement("INSERT INTO defections (lat,lng,time,filename,"
                                    + "category_id,user_id) VALUES(?,?,?,?,(Select category_id from defection_category "
                                    + "where category_name=?),(Select user_id from users where email=?))");
                            st.setDouble(1, point.getDouble("lat"));
                            st.setDouble(2, point.getDouble("lng"));
                            Date time = new SimpleDateFormat("yyyyMMdd_HHmmss").parse(obj.getString("photoTime"));
                            String timeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time);
                            st.setString(3, timeString);
                            st.setString(4, obj.getString("imageFileName"));
                            st.setString(5, obj.getString("category"));
                            st.setString(6, obj.getString("user"));
                            st.executeUpdate();

                        }
                    } catch (JSONException | ParseException ex) {
                        Logger.getLogger(UploadDefectionServlet.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(UploadDefectionServlet.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Extracts file name from HTTP header content-disposition
     */
    private String extractFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] items = contentDisp.split(";");
        for (String s : items) {
            if (s.trim().startsWith("filename")) {
                return s.substring(s.indexOf("=") + 2, s.length() - 1);
            }
        }
        return "";
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
