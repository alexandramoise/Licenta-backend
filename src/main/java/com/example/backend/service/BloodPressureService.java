package com.example.backend.service;

import com.example.backend.model.dto.request.BloodPressureRequestDto;
import com.example.backend.model.dto.response.BloodPressureResponseDto;
import com.example.backend.model.entity.BloodPressureType;
import com.example.backend.model.entity.table.Patient;
import com.example.backend.model.exception.EmptyList;
import com.example.backend.model.exception.ObjectNotFound;
import com.example.backend.model.exception.InvalidValues;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public interface BloodPressureService {
    /**
     * adding a new bloodpressure tracking result
     * it takes a BloodPressureRequestDto and formats it to fit into a BloodPressure entity structure
     * the new object is then saved in the database, finally converted into a BloodPressureResponseDto and returned.
     * @param bloodPressureRequestDto the BP tracking that will be added
     * @param patientEmail the email address of the patient who tracked their BP
     * @return bloodPressureResponseDto the formatted BloodPressureResponseDto objects that is the newly added BP tracking
     * @throws ObjectNotFound when there is no patient account with the specified email
     * @throws InvalidValues when the BP values do not correspond to a BP category
     */
    BloodPressureResponseDto addBloodPressure(BloodPressureRequestDto bloodPressureRequestDto, String patientEmail) throws ObjectNotFound, InvalidValues;

    /**
     * returns all the bloodpressure trackings of a patient
     * @param patientEmail the email address of the patient who is checking their BP history
     * @return list with all the trackings converted into BloodPressureResponseDto objects
     */
    List<BloodPressureResponseDto> getPatientBloodPressures(String patientEmail) throws ObjectNotFound;

    BloodPressureResponseDto getBloodPressureById(Long id, String patientEmail) throws ObjectNotFound, EmptyList;

    Page<BloodPressureResponseDto> getPagedBloodPressures(String patientEmail, Pageable pageable) throws ObjectNotFound;

    /**
     * updates a certain BP tracking, with the condition that only the most recent one can be edited
     * @param id the id of the BP tracking
     * @param bloodPressureRequestDto the Dto containing the information necessary for updating
     * @return the updated tracking converted to BloodPressureResponseDto type.
     */

    BloodPressureResponseDto updateBloodPressureById(Long id, BloodPressureRequestDto bloodPressureRequestDto);

    void deleteBloodPressureById(Long id);

    Map<Date, BloodPressureType> getPatientBPTendencyOverTime(String patientEmail) throws ObjectNotFound;

    void updatePatientType(Patient patient, BloodPressureType mostRecentType);

    /**
     * used to check the BP values in order to set its type
     * @param bloodPressureResponseDto the BP tracking that is analyzed
     * @throws InvalidValues when the BP values do not correspond to a BP category
     */
    void setBloodPressureType(BloodPressureResponseDto bloodPressureResponseDto) throws InvalidValues;
}
