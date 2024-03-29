package com.sap.cloud.sample.persistence;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.security.core.server.csi.IXSSEncoder;
import com.sap.security.core.server.csi.XSSEncoder;

/**
 * Servlet implementing a simple EJB based persistence sample application for SAP HANA Cloud Platform.
 */
public class PersistenceWithEJBServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceWithEJBServlet.class);

    @EJB
    private PersonBean personBean;

    /** {@inheritDoc} */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().println("<p>Persistence with EJB Sample!</p>");
        try {
            appendPersonTable(response);
            appendAddForm(response);
        } catch (Exception e) {
            response.getWriter().println("Persistence operation failed with reason: " + e.getMessage());
            LOGGER.error("Persistence operation failed", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        try {
            doAdd(request);
            doGet(request, response);
        } catch (Exception e) {
            response.getWriter().println("Persistence operation failed with reason: " + e.getMessage());
            LOGGER.error("Persistence operation failed", e);
        }
    }

    private void appendPersonTable(HttpServletResponse response) throws SQLException, IOException {
        // Append table that lists all persons
        List<Person> resultList = personBean.getAllPersons();
        response.getWriter().println(
                "<p><table border=\"1\"><tr><th colspan=\"3\">" + (resultList.isEmpty() ? "" : resultList.size() + " ")
                        + "Entries in the Database</th></tr>");
        if (resultList.isEmpty()) {
            response.getWriter().println("<tr><td colspan=\"3\">Database is empty</td></tr>");
        } else {
            response.getWriter().println("<tr><th>First name</th><th>Last name</th><th>Id</th></tr>");
        }
        IXSSEncoder xssEncoder = XSSEncoder.getInstance();
        int iterator = 1;
        for (Person p : resultList) {
            response.getWriter().println(
                    "<tr><td>" + xssEncoder.encodeHTML(p.getFirstName()) + "</td><td>"
                            + xssEncoder.encodeHTML(p.getLastName()) + "</td><td>" + iterator++ + "</td></tr>");
        }
        response.getWriter().println("</table></p>");
    }

    private void appendAddForm(HttpServletResponse response) throws IOException {
        // Append form through which new persons can be added
        response.getWriter().println(
                "<p><form action=\"\" method=\"post\">" + "First name:<input type=\"text\" name=\"FirstName\">"
                        + "&nbsp;Last name:<input type=\"text\" name=\"LastName\">"
                        + "&nbsp;<input type=\"submit\" value=\"Add Person\">" + "</form></p>");
    }

    private void doAdd(HttpServletRequest request) throws ServletException, IOException, SQLException {
        // Extract name of person to be added from request
        String firstName = request.getParameter("FirstName");
        String lastName = request.getParameter("LastName");

        // Add person if name is not null/empty
        if (firstName != null && lastName != null && !firstName.trim().isEmpty() && !lastName.trim().isEmpty()) {
            Person person = new Person();
            person.setFirstName(firstName.trim());
            person.setLastName(lastName.trim());
            personBean.addPerson(person);
        }
    }
}
