package Bai4;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@WebServlet(name = "uploadServlet", value = "/uploadServlet")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 50, // 50MB
        maxRequestSize = 1024 * 1024 * 5)// 50MB)
public class UploadServlet extends HttpServlet {
    String filename, extname;
    Part part;
    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");
        req.getRequestDispatcher("/html/upload.html").forward(req, resp); // render file html
//        resp.sendRedirect("./html/upload.html");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//            System.out.println(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()); // duong dan folder resources/root

        filename = req.getParameter("filename");
        String checkbox = req.getParameter("checkbox");
        Set<String> nameFileExists = getResourceFolderFiles("uploads");
        part = req.getPart("fileUpLoad");

        extname = extensionFile(part);

        boolean isExists = false;
        for (String nameFile : nameFileExists) {
            if (nameFile.equals("" + filename + extname)) {
                isExists = true;
                break;
            }
        }

        if (isExists){
            if (checkbox != null){
                uploadFile(req, resp, "File has been overridden");
            }else {
                req.setAttribute("status", "warning");
                req.setAttribute("message", "File already exists");
                req.getRequestDispatcher("/jsp/logMessage_Forward.jsp").forward(req, resp);
            }
        }else {
            uploadFile(req, resp, "File has been uploaded");
        }
    }

    private void uploadFile(HttpServletRequest req, HttpServletResponse resp, String message) throws IOException, ServletException {
        if (extname.equals(".txt") || extname.equals(".doc") || extname.equals(".docx") || extname.equals(".png")
                || extname.equals(".jpg") || extname.equals(".pdf") || extname.equals(".rar") || extname.equals(".zip")) {
            part.write(this.getFolderUpload("/uploads").getAbsolutePath() + File.separator + filename + extname);

            req.setAttribute("status", "success");
            req.setAttribute("message", message);
        }else {
            req.setAttribute("status", "warning");
            req.setAttribute("message", "Unsupported file extension");
        }
        req.getRequestDispatcher("/jsp/logMessage_Forward.jsp").forward(req, resp);
    }
    private Set<String> getResourceFolderFiles (String folder) {
        URL url = getServletContext().getClassLoader().getResource(folder);
//        System.out.println("url: " + url);

        try {
            return Stream.of(new File(url.toURI()).listFiles())
                    .filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .collect(Collectors.toSet());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

//     Extracts file name from HTTP header content-disposition
    private String extractFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] items = contentDisp.split(";");
        for (String s : items) {
            if (s.trim().startsWith("filename")) {
                return s.substring(s.indexOf("=") + 2, s.length() - 1);
            }
        }
        return "";

//        Solution2----
//        String filename = Path.of(part.getSubmittedFileName()).getFileName().toString(); // lay ten cua fileUpload
    }
//    get extension file upload (png, jpg,...)
    private String extensionFile(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] items = contentDisp.split(";");
        for (String s : items) {
            if (s.trim().startsWith("filename")) {
                return s.substring(s.indexOf("."), s.length() - 1);
            }
        }
        return "";
    }
    public File getFolderUpload(String pathFolder) {
        URL url = Objects.requireNonNull(getServletContext().getClassLoader().getResource(pathFolder));
        File folderUpload;
        try {
            folderUpload = new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        if (!folderUpload.exists()) {
            folderUpload.mkdirs();
        }
        return folderUpload;
    }
//    https://www.tabnine.com/code/java/methods/javax.servlet.http.Part/getContentType
//    https://www.youtube.com/watch?v=BXJLoDILt50
}
