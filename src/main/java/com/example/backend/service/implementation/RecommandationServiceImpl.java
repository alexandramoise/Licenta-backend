package com.example.backend.service.implementation;

import com.example.backend.model.dto.request.RecommandationRequestDto;
import com.example.backend.model.dto.response.RecommandationResponseDto;
import com.example.backend.model.entity.Doctor;
import com.example.backend.model.entity.Recommandation;
import com.example.backend.model.exception.ObjectNotFound;
import com.example.backend.model.repo.DoctorRepo;
import com.example.backend.model.repo.RecommandationRepo;
import com.example.backend.service.RecommandationService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommandationServiceImpl implements RecommandationService {
    private final DoctorRepo doctorRepo;
    private final RecommandationRepo recommandationRepo;
    private final ModelMapper modelMapper;

    public RecommandationServiceImpl(DoctorRepo doctorRepo, RecommandationRepo recommandationRepo, ModelMapper modelMapper) {
        this.doctorRepo = doctorRepo;
        this.recommandationRepo = recommandationRepo;
        this.modelMapper = modelMapper;
    }

    @Override
    public RecommandationResponseDto addRecomandation(String doctorEmail, RecommandationRequestDto recommandationRequestDto) {
        Doctor doctor = doctorRepo.findByEmail(doctorEmail).orElseThrow(() -> new ObjectNotFound("No doctor with this email"));

        Recommandation recommandation = new Recommandation();
        recommandation.setText(recommandationRequestDto.getText());
        recommandation.setHashtag(recommandationRequestDto.getHashtag());
        recommandation.setRecommandationType(recommandationRequestDto.getRecommandationType());
        recommandation.setDoctor(doctor);
        recommandationRepo.save(recommandation);

        RecommandationResponseDto result = modelMapper.map(recommandation, RecommandationResponseDto.class);
        result.setId(recommandation.getId());
        return result;
    }

    @Override
    public Page<RecommandationResponseDto> getAllRecommandations(String doctorEmail, Pageable pageable) {
        Doctor doctor = doctorRepo.findByEmail(doctorEmail).orElseThrow(() -> new ObjectNotFound("No doctor with this email"));

        Page<Recommandation> recommandations = recommandationRepo.findAll(doctorEmail, pageable);
        List<RecommandationResponseDto> result = recommandations
                .getContent()
                .stream()
                .map(r -> {
                    return modelMapper.map(r, RecommandationResponseDto.class);
                }).collect(Collectors.toList());
        return new PageImpl<>(result, pageable, recommandations.getTotalElements());
    }

    @Override
    public Page<RecommandationResponseDto> getAllRecommandationsByHashtag(String doctorEmail, String hashtag, Pageable pageable) {
        Doctor doctor = doctorRepo.findByEmail(doctorEmail).orElseThrow(() -> new ObjectNotFound("No doctor with this email"));

        Page<Recommandation> recommandations = recommandationRepo.findByHashtag(doctorEmail, hashtag, pageable);
        List<RecommandationResponseDto> result = recommandations
                .getContent()
                .stream()
                .map(r -> {
                    return modelMapper.map(r, RecommandationResponseDto.class);
                }).collect(Collectors.toList());
        return new PageImpl<>(result, pageable, recommandations.getTotalElements());
    }

    @Override
    public Page<RecommandationResponseDto> getRecommandationsForType(String doctorEmail, String recommandationType, Pageable pageable) {
        Doctor doctor = doctorRepo.findByEmail(doctorEmail).orElseThrow(() -> new ObjectNotFound("No doctor with this email"));

        Page<Recommandation> recommandations = recommandationRepo.findByType(doctorEmail, recommandationType, pageable);
        List<RecommandationResponseDto> result = recommandations
                .getContent()
                .stream()
                .map(r -> {
                    return modelMapper.map(r, RecommandationResponseDto.class);
                }).collect(Collectors.toList());
        return new PageImpl<>(result, pageable, recommandations.getTotalElements());
    }

    @Override
    public Page<RecommandationResponseDto> getRecommandationsByHashtagForType(String doctorEmail, String hashtag, String recommandationType, Pageable pageable) {
        Doctor doctor = doctorRepo.findByEmail(doctorEmail).orElseThrow(() -> new ObjectNotFound("No doctor with this email"));

        Page<Recommandation> recommandations = recommandationRepo.findByHashtagAndType(doctorEmail, hashtag, recommandationType, pageable);
        List<RecommandationResponseDto> result = recommandations
                .getContent()
                .stream()
                .map(r -> {
                    return modelMapper.map(r, RecommandationResponseDto.class);
                }).collect(Collectors.toList());
        return new PageImpl<>(result, pageable, recommandations.getTotalElements());
    }

    @Override
    public Page<RecommandationResponseDto> getAllRecommandationsForPatient(String doctorEmail, String type, Pageable pageable) {
        Doctor doctor = doctorRepo.findByEmail(doctorEmail).orElseThrow(() -> new ObjectNotFound("No doctor with this email"));

        Page<Recommandation> recommandations = recommandationRepo.findAllForPatient(doctorEmail, type, pageable);
        List<RecommandationResponseDto> result = recommandations
                .getContent()
                .stream()
                .map(r -> {
                    return modelMapper.map(r, RecommandationResponseDto.class);
                }).collect(Collectors.toList());
        return new PageImpl<>(result, pageable, recommandations.getTotalElements());
    }

    @Override
    public Page<RecommandationResponseDto> getByTagForPatient(String doctorEmail, String hashtag, String type, Pageable pageable) {
        Doctor doctor = doctorRepo.findByEmail(doctorEmail).orElseThrow(() -> new ObjectNotFound("No doctor with this email"));

        Page<Recommandation> recommandations = recommandationRepo.findByTagForPatient(doctorEmail, hashtag, type, pageable);
        List<RecommandationResponseDto> result = recommandations
                .getContent()
                .stream()
                .map(r -> {
                    return modelMapper.map(r, RecommandationResponseDto.class);
                }).collect(Collectors.toList());
        return new PageImpl<>(result, pageable, recommandations.getTotalElements());
    }
}
