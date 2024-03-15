package org.openmrs.module.ugandaemr.web.resource;

import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + GPResource.UGANDAEMR + GPResource.DEFINITION)

public class GPResource {
    public static final String UGANDAEMR = "/ugandaemr";
    public static final String DEFINITION = "/gp";

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Object evaluate(@RequestParam(required = true, value = "property") String propertyName) {
        AdministrationService administrationService = Context.getAdministrationService();

        String query = "select property_value from global_property where property = '"+ propertyName + "';";


        List<List<Object>> result = administrationService.executeSQL(query, true);
        Object ff=null;
        if (!result.isEmpty()) {
            for (List<Object> object : result) {
                ff= object.get(0);
            }
        }

        return new ResponseEntity<>(ff, HttpStatus.OK);

    }

}
