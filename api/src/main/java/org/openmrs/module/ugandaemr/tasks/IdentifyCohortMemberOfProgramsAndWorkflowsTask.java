package org.openmrs.module.ugandaemr.tasks;

import org.openmrs.*;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.CohortService;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.ArrayList;
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
          //  e.printStackTrace();
        }
    }

    private void hivEligibilityCohort() {
        AdministrationService administrationService = Context.getAdministrationService();

        try {
            System.out.println("Executing hivEligibilityCohort...");
            List<Object> persons = Collections.singletonList(administrationService.executeSQL(HIV_ELIGIBILITY_QUERY, true));
            enrollPatientInCohort(persons, HIV_ELIGIBILITY_COHORT_UUID);
        } catch (Exception e) {
         //   e.printStackTrace();
        }
    }

    private void enrollPatientInCohort(List<Object> list, String cohortUuid) {
        CohortService cohortService = Context.getCohortService();
        Cohort cohort = cohortService.getCohortByUuid(cohortUuid);

        for (Object item : list) {
            try {
                ArrayList<?> arrayList = (ArrayList<?>) item;
                Integer patientId = ((Integer) ((ArrayList) ((ArrayList) list.get(0)).get(0)).get(0)).intValue();
                Patient patient = Context.getPatientService().getPatient(patientId);

                cohortService.addPatientToCohort(cohort, patient);

                // Insert into cohort_member table
                insertIntoCohortMember(patientId, cohort.getId());
            } catch (Exception e) {
               // e.printStackTrace();
            }
        }
    }

    private void insertIntoCohortMember(int patientId, int cohortId) {
        AdministrationService administrationService = Context.getAdministrationService();

        try {
            if (!isMemberInCohort(patientId, cohortId)) {
                System.out.println("Inserting into cohort_member table...");
                String sql = "INSERT INTO cohort_member (patient_id, cohort_id, start_date, end_date, creator, date_created, voided, voided_by, date_voided, void_reason, uuid, date_changed, changed_by)" +
                        " VALUES (" +
                        patientId + ", " +
                        cohortId + ", " +
                        "NOW(), " +
                        "null, " +
                        1 + ", " +
                        "NOW(), 0, null, null, null, " +
                        "UUID() AS uuid, null, null)";

                administrationService.executeSQL(sql, false);
            } else {
                System.out.println("Member already exists in cohort_member table. Skipping insertion.");
            }
        } catch (Exception e) {
          //  e.printStackTrace();
        }
    }

    private boolean isMemberInCohort(int patientId, int cohortId) {
        AdministrationService administrationService = Context.getAdministrationService();

        try {
            String checkSql = "SELECT COUNT(*) FROM cohort_member WHERE patient_id = " + patientId + " AND cohort_id = " + cohortId;
            Number count = (Number) administrationService.executeSQL(checkSql, true).get(0).get(0);

            return count != null && count.intValue() > 0;
        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        }
    }

}
