package org.openmrs.module.ugandaemr.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.*;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.CohortService;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.openmrs.module.ugandaemr.UgandaEMRConstants.*;


public class IdentifyCohortMemberOfProgramsAndWorkflowsTask extends AbstractTask {

    private Log log = LogFactory.getLog(this.getClass());

    @Override
    public void execute() {
        try {
            // TB HIV eligibility
            hivEligibilityCohort(TB_ELIGIBILITY_QUERY,TB_ELIGIBILITY_COHORT_UUID);
            // HIV eligibility
            hivEligibilityCohort(HIV_ELIGIBILITY_QUERY, HIV_ELIGIBILITY_COHORT_UUID);
        } catch (Exception e) {
            log.error(e);
        }
    }

    private void hivEligibilityCohort(String query, String cohortUuid) {
        AdministrationService administrationService = Context.getAdministrationService();
        try {
            List<Object> persons = Collections.singletonList(administrationService.executeSQL(query, true));
            enrollPatientInCohort(persons, cohortUuid);
        } catch (Exception e) {
            log.error(e);
        }
    }

    private void enrollPatientInCohort(List<Object> list, String cohortUuid) {
        CohortService cohortService = Context.getCohortService();
        Cohort cohort = cohortService.getCohortByUuid(cohortUuid);

        for (Object item : list) {
            Integer patientId = Integer.parseInt(item.toString());
            try {

                Patient patient = Context.getPatientService().getPatient(patientId);
                cohortService.addPatientToCohort(cohort, patient);

                insertIntoCohortMember(patientId, cohort.getId());
            } catch (Exception e) {
                log.error(log);
            }
        }
    }

    private void insertIntoCohortMember(int patientId, int cohortId) {
        AdministrationService administrationService = Context.getAdministrationService();

        try {
            if (!isMemberInCohort(patientId, cohortId)) {
                CohortService cohortService = Context.getCohortService();
                cohortService.addPatientToCohort(cohortService.getCohort(cohortId), Context.getPatientService().getPatient(patientId));
            }
        } catch (Exception e) {
            log.error(log);
        }
    }

    private boolean isMemberInCohort(int patientId, int cohortId) {
        List<Cohort> cohorts = Context.getCohortService().getCohortsContainingPatientId(patientId);
        List<Cohort> cohortList = cohorts.stream().filter(cohort -> {
            cohort.getCohortId();
            return false;
        }).collect(Collectors.toList());

        return cohortList.size() > 0;
    }

}
