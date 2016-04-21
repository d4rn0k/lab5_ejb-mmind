import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;

/**
 * Servlet class to handle request and play in MasterMind.
 *
 * @author Konrad Szwedo
 * @version 0.5L
 */
public class MGame extends HttpServlet {

    @EJB
    IMasterMind masterMind;
    private static final long serialVersionUID = 123L;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws javax.naming.NamingException
     */
    private void processRequest(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException, NamingException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        int n;  // Colors number
        int k;  // Pegs number
        long s; // random seed

        try {
            n = Integer.parseInt(request.getParameter("n"));
            k = Integer.parseInt(request.getParameter("k"));
            s = Long.parseLong(request.getParameter("s"));

        } catch (NumberFormatException exc) {
            n = 8;
            k = 4;
            s = 100L;
        }

        masterMind.initialize(n, k, s);
        PrintWriter out = response.getWriter();
        try {
            out.println(masterMind.play());
            out.close();
        } catch(Exception exc) {
            out.println(n);
            out.close();
        }
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
        try {
            processRequest(request, response);
        } catch (NamingException ex) {
            Logger.getLogger(MGame.class.getName()).log(Level.SEVERE, null, ex);
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
        try {
            processRequest(request, response);
        } catch (NamingException ex) {
            Logger.getLogger(MGame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
