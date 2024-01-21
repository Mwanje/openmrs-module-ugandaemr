package org.openmrs.module.ugandaemr.tasks;

import org.openmrs.*;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.CohortService;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.Collections;
import java.util.List;

import static org.openmrs.module.ugandaemr.UgandaEMRConstants.HIV_ELIGIBILITY_COHORT_UUID;
import static org.openmrs.module.ugandaemr.UgandaEMRConstants.HIV_ELIGIBILITY_QUERY;


public class IdentifyCohortMemberOfProgramsAndWorkflowsTask extends AbstractTask{

    @Override
    public void execute() {
        try {
            hivEligibilityCohort();
        } catch (Exception e) {
            // Handle or log the exception appropriately
            e.printStackTrace();
        }
    }

    private void hivEligibilityCohort() {
        AdministrationService administrationService = Context.getAdministrationService();

        try {
            List<Object> persons = Collections.singletonList(administrationService.executeSQL(HIV_ELIGIBILITY_QUERY, true));
            enrollPatientInCohort(persons, HIV_ELIGIBILITY_COHORT_UUID);
        } catch (Exception e) {
            // Handle or log the exception appropriately
            e.printStackTrace();
        }
    }

    private void enrollPatientInCohort(List<Object> list, String cohortUuid) {
        CohortService cohortService = Context.getCohortService();
        Cohort cohort = cohortService.getCohortByUuid(cohortUuid);

        for (Object item : list) {
            try {
                Patient patient = Context.getPatientService().getPatient((Integer) item);
                cohortService.addPatientToCohort(cohort, patient);
            } catch (Exception e) {
                // Handle or log the exception appropriately
                e.printStackTrace();
            }
        }
    }
}
