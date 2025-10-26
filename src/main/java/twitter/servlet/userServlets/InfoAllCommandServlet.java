package twitter.servlet.userServlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import twitter.configuration.ComponentFactory;
import twitter.controller.v2.InfoController;
import twitter.dto.v2.response.InfoResponseDto;
import twitter.exceptions.TwitterIllegalArgumentException;
import twitter.sideComponents.web.ObjectMapperAsComponent;

import java.io.IOException;
import java.util.List;

public class InfoAllCommandServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectMapper mapper = ComponentFactory.getComponent(ObjectMapperAsComponent.class).getObjectMapper();

        try {

            InfoController infoController = ComponentFactory.getComponent(InfoController.class);
            List<InfoResponseDto> infoResponseDto = infoController.infoAll();
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");

            resp.getWriter().write(mapper.writeValueAsString(infoResponseDto));

        } catch (TwitterIllegalArgumentException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(mapper.writeValueAsString(ex.getMessage()));
        } catch (Exception ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(mapper.writeValueAsString(ex.getMessage()));
        }
    }
}
