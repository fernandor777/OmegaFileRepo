/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.services.ejbs.censocat;

import com.origami.sgm.entities.FotoPredio;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.math.NumberUtils;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author Fernando, Carlos Loor Vargas
 */
@WebServlet(name = "FotosServlet", urlPatterns = {"/FotosServlet"})
public class FotosServlet extends HttpServlet {

    @Inject
    private UploadFotoBean uploadFotoBean;
    @Inject
    private OmegaUploader omegaUploader;
    @javax.inject.Inject
    private FotosService fotosService;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    public void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        this.doPost(request, response);
    }

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
        // id de foto
        if (request.getParameter("fotoId") != null && NumberUtils.isNumber(request.getParameter("fotoId"))) {
            Long fotoId = Long.parseLong(request.getParameter("fotoId"));
            FotoPredio fotop = fotosService.loadFoto(fotoId);
            // content-type
            response.setContentType(fotop.getContentType());
            // escribir el output-stream del response
            if (fotop.getFileOid() != null) {
                OutputStream os = response.getOutputStream();
                omegaUploader.streamFile(fotop.getFileOid(), os);
                //osAux = os;
                os.close();
            }
        }
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
        /**
         * Detectar si es un id long o UUID
         */
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            return;
        }

        // si es un entero es ID long
        if (idParam.matches("^[\\d]+$")) {
            postWithNumId(request, response);
        }
    }

    protected void postWithNumId(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        FotoUploadRespMod respModel = new FotoUploadRespMod(false, null);
        try {
            response.setContentType("application/json;charset=UTF-8");
            this.genFactory();
            Long id = Long.parseLong(request.getParameter("id"));
            uploadFotoBean.setPredioId(id);
            boolean isMultipart = ServletFileUpload.isMultipartContent(request);
            ServletFileUpload upload = new ServletFileUpload(uploadFotoBean.getFactory());
            upload.setFileSizeMax(10000000); // max 10MB
            List<FileItem> items = upload.parseRequest(request);
            Iterator<FileItem> iter = items.iterator();
            while (iter.hasNext()) {
                FileItem item = iter.next();
                if (item.isFormField()) {
                } else {
                    InputStream is = item.getInputStream();
                    Long fileId = omegaUploader.uploadFile(is, item.getName(), item.getContentType());
                    uploadFotoBean.setFileId(fileId);
                    uploadFotoBean.setNombre(item.getName());
                    uploadFotoBean.setContentType(item.getContentType());
                    uploadFotoBean.saveFotoId();
                    respModel.setFotoId(uploadFotoBean.getFotoPredioId());
                    respModel.setOk(true);
                    is.close();
                    break;
                }
            }
        } catch (FileUploadException ex) {
            Logger.getLogger(FotosServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NumberFormatException | IOException ex) {
            Logger.getLogger(FotosServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        ObjectMapper mapper = new ObjectMapper();
        String jsonResp = mapper.writeValueAsString(respModel);
        response.getWriter().write(jsonResp);
    }

    protected void genFactory() {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        if (this.getServletConfig() != null && this.getServletConfig().getServletContext() != null) {
            ServletContext servletContext = this.getServletConfig().getServletContext();
            File repository = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
            factory.setRepository(repository);
            uploadFotoBean.setFactory(factory);
        }
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
