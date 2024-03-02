package org.openmrs.module.ugandaemr.web.resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.joda.time.DateTime;
import org.joda.time.Weeks;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationService;
import org.openmrs.calculation.result.CalculationResult;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.metadatadeploy.descriptor.ProgramDescriptor;
import org.openmrs.module.ugandaemr.UgandaEMRConstants;
import org.openmrs.module.ugandaemr.utils.EncounterBasedRegimenUtils;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.apache.http.client.utils.DateUtils.formatDate;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1)
public class CareProgramResource extends BaseRestController {

    protected final Log log = LogFactory.getLog(getClass());
    static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    @Autowired
    //private ProgramManager programManager;

    public static String HIV_PROGRAM_UUID = "dfdc6d40-2f2f-463d-ba90-cc97350441a8";

    public static final Locale LOCALE = Locale.ENGLISH;

    public String name = null;


    /**
     * Fetches default facility
     *
     * @return custom location object
     */
    @RequestMapping(method = RequestMethod.GET, value = "/default-facility")
    @ResponseBody
    public Object getDefaultConfiguredFacility() {
        GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(UgandaEMRConstants.GP_DEFAULT_LOCATION);

        if (gp == null) {
            return new ResponseEntity<Object>("Default facility not configured!", new HttpHeaders(), HttpStatus.NOT_FOUND);
        }

        Location location = (Location) gp.getValue();
        ObjectNode locationNode = JsonNodeFactory.instance.objectNode();

        locationNode.put("locationId", location.getLocationId());
        locationNode.put("uuid", location.getUuid());
        locationNode.put("display", location.getName());

        return locationNode.toString();

    }

    /**
     * Returns regimen history for a patient
     * @param category // ARV or TB
     * @param patientUuid
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/regimenHistory")
    @ResponseBody
    public Object getRegimenHistory(@RequestParam("patientUuid") String patientUuid, @RequestParam("category") String category) {
        ObjectNode regimenObj = JsonNodeFactory.instance.objectNode();
        if (StringUtils.isBlank(patientUuid)) {
            return new ResponseEntity<Object>("You must specify patientUuid in the request!",
                    new HttpHeaders(), HttpStatus.BAD_REQUEST);
        }

        Patient patient = Context.getPatientService().getPatientByUuid(patientUuid);

        if (patient == null) {
            return new ResponseEntity<Object>("The provided patient was not found in the system!",
                    new HttpHeaders(), HttpStatus.NOT_FOUND);
        }
        ArrayNode regimenNode = JsonNodeFactory.instance.arrayNode();
        List<SimpleObject> obshistory = EncounterBasedRegimenUtils.getRegimenHistoryFromObservations(patient, category);
        for (SimpleObject obj : obshistory) {
            ObjectNode node = JsonNodeFactory.instance.objectNode();;
            node.put("startDate", obj.get("startDate").toString());
            node.put("endDate", obj.get("endDate").toString());
            node.put("regimenShortDisplay", obj.get("regimenShortDisplay").toString());
            node.put("regimenLine", obj.get("regimenLine").toString());
            node.put("regimenLongDisplay", obj.get("regimenLongDisplay").toString());
            node.put("changeReasons", obj.get("changeReasons").toString());
            node.put("regimenUuid", obj.get("regimenUuid").toString());
            node.put("current", obj.get("current").toString());
            regimenNode.add(node);
        }

        regimenObj.put("results", regimenNode);
        return regimenObj.toString();

    }

    /**
     * Gets a list of flags for a patient
     * @param request
     * @param patientUuid
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/flags") // gets all flags for a patient
    @ResponseBody
    public Object getAllPatientFlags(HttpServletRequest request, @RequestParam("patientUuid") String patientUuid) {
        if (StringUtils.isBlank(patientUuid)) {
            return new ResponseEntity<Object>("You must specify patientUuid in the request!",
                    new HttpHeaders(), HttpStatus.BAD_REQUEST);
        }

        Patient patient = Context.getPatientService().getPatientByUuid(patientUuid);
        ObjectNode flagsObj = JsonNodeFactory.instance.objectNode();

        if (patient == null) {
            return new ResponseEntity<Object>("The provided patient was not found in the system!",
                    new HttpHeaders(), HttpStatus.NOT_FOUND);
        }

        ArrayNode flags = JsonNodeFactory.instance.arrayNode();
        flagsObj.put("results", flags);

        return flagsObj.toString();

    }















}
