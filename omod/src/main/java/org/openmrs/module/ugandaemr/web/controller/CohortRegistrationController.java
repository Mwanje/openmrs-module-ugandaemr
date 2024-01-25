package org.openmrs.module.ugandaemr.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.CohortService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemr.UgandaEMRConstants;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;


@Controller
public class CohortRegistrationController {

	private final Log log = LogFactory.getLog(CohortRegistrationController.class);

	@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/" + UgandaEMRConstants.UGANDAEMR_MODULE_ID
			+ "/cohort/delete", method = RequestMethod.GET)
	@ResponseBody
	public SimpleObject deleteCohort(
			@RequestParam(required = true, value = "uuid") String cohortUuid) {

		Cohort cohort = Context.getCohortService().getCohortByUuid(cohortUuid);
		SimpleObject response = new SimpleObject();
		Context.getCohortService().voidCohort(cohort,"retired");


		response.add("message", "Cohort Deleted");

		return response;
	}

	@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/" + UgandaEMRConstants.UGANDAEMR_MODULE_ID
			+ "/cohort/edit", method = RequestMethod.GET)
	@ResponseBody
	public SimpleObject editCohort(
			@RequestParam(required = true, value = "uuid") String cohortUuid) {

		Cohort cohort = Context.getCohortService().getCohortByUuid(cohortUuid);
		SimpleObject response = new SimpleObject();

		response.add("name", cohort.getName());
		response.add("description", cohort.getDescription());
		response.add("uuid", cohort.getUuid());

		return response;
	}

	@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/" + UgandaEMRConstants.UGANDAEMR_MODULE_ID
			+ "/cohort/saveEdit", method = RequestMethod.POST)
	@ResponseBody
	public SimpleObject saveEditedCohort(HttpServletRequest request,@RequestBody String body,
			@RequestParam(required = true, value = "uuid") String cohortUuid) {

		Cohort cohort = Context.getCohortService().getCohortByUuid(cohortUuid);

		JSONObject newBody  = new JSONObject(body);
		cohort.setName(newBody.getString("name"));
		cohort.setDescription(newBody.getString("description"));
		cohort.setUuid(newBody.getString("uuid"));

		Context.getCohortService().saveCohort(cohort);

		SimpleObject response = new SimpleObject();

		response.add("name", cohort.getName());
		response.add("description", cohort.getDescription());
		response.add("uuid", cohort.getUuid());

		return response;
	}

	@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/" + UgandaEMRConstants.UGANDAEMR_MODULE_ID
			+ "/patientCohorts", method = RequestMethod.GET)
	@ResponseBody
	public List<SimpleObject> getCohortsByPatientUuid(
			@RequestParam(required = true, value = "patientUuid") String patientUuid) {

		List<SimpleObject> cohortsList = new ArrayList<>();

		Patient patient = Context.getPatientService().getPatientByUuid(patientUuid);

		if (patient != null) {
			// Get the patient's cohorts
			List<List<Object>> patientCohorts = getCohortsByPatient(patient);

			for (List<Object> cohort : patientCohorts) {
				SimpleObject cohortObject = new SimpleObject();
				cohortObject.add("cohort_id", cohort.get(0));
				cohortObject.add("name", cohort.get(1));
				cohortObject.add("description", cohort.get(2));
				cohortsList.add(cohortObject);
			}
		}

		return cohortsList;
	}

	private List<List<Object>> getCohortsByPatient(Patient patient) {
		CohortService cohortService = Context.getCohortService();
		AdministrationService administrationService = Context.getAdministrationService();
		String hqlQuery = "select c.cohort_id, c.name, c.description FROM cohort c\n" +
				"    inner join cohort_member cm on (c.cohort_id=cm.cohort_id)\n" +
				"        inner join person p on (cm.patient_id=p.person_id)\n" +
				"         where cm.voided=0 and p.uuid="+patient.getUuid();

		List res = administrationService.executeSQL(hqlQuery, true);

		return res;

	}

}
