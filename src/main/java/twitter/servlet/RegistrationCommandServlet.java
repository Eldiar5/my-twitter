package twitter.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import twitter.configuration.ComponentFactory;
import twitter.controller.v2.RegistrationController;
import twitter.dto.v2.request.RegistrationRequestDto;
import twitter.dto.v2.response.RegistrationResponseDto;
import twitter.exceptions.TwitterIllegalArgumentException;
import twitter.sideComponents.web.ObjectMapperAsComponent;

import java.io.IOException;

public class RegistrationCommandServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectMapper mapper = ComponentFactory.getComponent(ObjectMapperAsComponent.class).getObjectMapper();
        RegistrationRequestDto requestDto = mapper.readValue(req.getInputStream(), RegistrationRequestDto.class);

        try {
            RegistrationController regController = ComponentFactory.getComponent(RegistrationController.class);
            RegistrationResponseDto regResponseDto = regController.register(requestDto);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");

            resp.getWriter().write(mapper.writeValueAsString(regResponseDto));
        } catch (TwitterIllegalArgumentException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(mapper.writeValueAsString(ex.getMessage()));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(mapper.writeValueAsString(e.getMessage()));
        }
    }
}
