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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

	public Object getCohortsByPatientUuid(
			@RequestParam(required = true, value = "patientUuid") String patientUuid) {

		List<SimpleObject> cohortsList = new ArrayList<>();

		try {
			Patient patient = Context.getPatientService().getPatientByUuid(patientUuid);

			if (patient != null) {
				// Get the patient's cohorts
				List<List<Object>> patientCohorts = getCohortsByPatient(patient);

				for (List<Object> cohort : patientCohorts) {
					SimpleObject cohortObject = new SimpleObject();
					cohortObject.add("cohort_id", cohort.get(0));
					cohortObject.add("name", cohort.get(1));
					cohortObject.add("description", cohort.get(2));

					// Handling enrollmentFormUuid and discontinuationFormUuid
					handleCohortSpecifics(cohort, cohortObject);

					cohortsList.add(cohortObject);
				}
			} else {
				// Handle the case where the patient is not found
				return new ResponseEntity<>("Patient not found", HttpStatus.NOT_FOUND);
			}

		} catch (Exception e) {
			// Handle exceptions
			return new ResponseEntity<>("Error processing request", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<>(cohortsList, HttpStatus.OK);
	}

	private void handleCohortSpecifics(List<Object> cohort, SimpleObject cohortObject) {
		String cohortName = cohort.get(1).toString();

		// Use constants or enums for cohort names
		if ("HIV Program".equalsIgnoreCase(cohortName)) {
			cohortObject.add("uuid", "18c6d4aa-0a36-11e7-8dbb-507b9dc4c741");
			cohortObject.add("enrollmentFormUuid", "b21e38da-79b0-489f-9e2e-49fa0a562531");
			cohortObject.add("discontinuationFormUuid", "");
		} else if ("TB Program".equalsIgnoreCase(cohortName)) {
			cohortObject.add("uuid", "9dc21a72-0971-11e7-8037-507b9dc4c741");
			cohortObject.add("enrollmentFormUuid", "ce38db94-ce38-4967-850b-efe485da4889");
			cohortObject.add("discontinuationFormUuid", "");
		} else if ("MCH Program".equalsIgnoreCase(cohortName)) {
			cohortObject.add("uuid", "5e8c094c-0a36-11e7-b779-507b9dc4c741");
			cohortObject.add("enrollmentFormUuid", "");
			cohortObject.add("discontinuationFormUuid", "");
		}else {
			cohortObject.add("uuid", "4d47e58c-01c5-4ecd-8cb0-7c42850eeb7a");
			cohortObject.add("enrollmentFormUuid", "8bda8f3b-68a3-4f8d-bfab-916648df95b2");
			cohortObject.add("discontinuationFormUuid", "0aafcba3-fd25-4563-99dc-511c442b9be0");
		}
	}

	private List<List<Object>> getCohortsByPatient(Patient patient) {
		CohortService cohortService = Context.getCohortService();
		AdministrationService administrationService = Context.getAdministrationService();
		String hqlQuery = "select distinct c.cohort_id, c.name, c.description FROM cohort c\n" +
				"    inner join cohort_member cm on (c.cohort_id=cm.cohort_id)\n" +
				"        inner join person p on (cm.patient_id=p.person_id)\n" +
				"       inner join cohort_type ct on (c.cohort_type_id = ct.cohort_type_id)\n" +
				"         where cm.voided=0 and ct.uuid='1345c89c-8463-41e9-9cd0-8c14aa255ba8' and p.uuid='"+patient.getUuid()+"'";

		List res = administrationService.executeSQL(hqlQuery, true);

		return res;

	}
}
