package com.marklogic;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mlcp")
public class MlcpApiController {
    
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MlcpApiController.class);
    private static final String MLCP_CONFIG_FILENAME = "mlcpOptions.txt";
  
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> getMlcpConfig(HttpServletRequest req) {

      Enumeration<String> parameterNames = req.getParameterNames();
      Map<String, String> params = new HashMap<String, String>();

      while (parameterNames.hasMoreElements()) {
          String paramName = parameterNames.nextElement();
          String paramValue = req.getParameter(paramName);
          params.put(paramName, paramValue);
          LOGGER.debug("* "+paramName +":"+ paramValue);
      }
      
      return params;
    }
    
    @RequestMapping(value = "download", method = RequestMethod.POST, produces = { MediaType.TEXT_PLAIN_VALUE })
    public ResponseEntity<InputStreamResource> downloadMlcpConfig(HttpServletRequest req) {

      Map<String, String> params = this.getMlcpConfig(req);
      String mlcpConfigContent = buildMlcpConfigContent(params);
      byte[] contentBytes = mlcpConfigContent.getBytes(StandardCharsets.UTF_8);
      InputStream inputStream = new ByteArrayInputStream(contentBytes);
      HttpHeaders headers = new HttpHeaders();
      headers.add("content-disposition", "attachment; filename=" + MLCP_CONFIG_FILENAME);
      return ResponseEntity
              .ok()
              .contentLength(contentBytes.length)
              .contentType(MediaType.TEXT_PLAIN)
              .headers(headers)
              .body(new InputStreamResource(inputStream));
    }

    /**
     * An options file should have the following contents:
     * 1. Each line contains either a command name, an option, or an option value, ordered as they would appear on the command line.
     * 2. Comments begin with “#” and must be on a line by themselves.
     * 3. Blank lines, leading whitespace, and trailing whitespace are ignored.
     */
    private String buildMlcpConfigContent(Map<String, String> params) {
        StringBuilder mlcpConfigBuilder = new StringBuilder();
        for (String paramKey : params.keySet()) {
            mlcpConfigBuilder.append(paramKey);
            mlcpConfigBuilder.append("\n");
            mlcpConfigBuilder.append(params.get(paramKey));
            mlcpConfigBuilder.append("\n");
        }
        return mlcpConfigBuilder.toString();
    }
}
