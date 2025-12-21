package com.example.student_activity_points.service;
import com.example.student_activity_points.domain.Requests;
import com.example.student_activity_points.domain.StudentActivity;
import com.example.student_activity_points.repository.RequestsRepository;
import com.example.student_activity_points.dto.TrackingDTO;
import com.example.student_activity_points.repository.StudentActivityRepository;
import org.springframework.stereotype.Service;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.stream.Collectors;
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring repositories are safe to inject directly")

@Service
public class TrackingService {
    private final RequestsRepository requestsRepository;
    private final StudentActivityRepository studentActivityRepository;

    public TrackingService(RequestsRepository requestsRepository, StudentActivityRepository studentActivityRepository) {
        this.requestsRepository = requestsRepository;
        this.studentActivityRepository = studentActivityRepository;
    }

    public TrackingDTO getTrackingBySid(String sid) {
        List<Requests> requests = requestsRepository.findBySid(sid);
        List<StudentActivity> activities = studentActivityRepository.findBySid(sid);

        TrackingDTO dto = new TrackingDTO();

        // Map Requests
        List<TrackingDTO.RequestDTO> reqDtos = requests.stream().map(r -> {
            TrackingDTO.RequestDTO rd = new TrackingDTO.RequestDTO();
            rd.setRid(r.getRid());
            rd.setActivityName(r.getActivityName());
            rd.setDescription(r.getDescription());
            rd.setActivityDate(r.getActivityDate());
            rd.setStatus(r.getStatus().name());   // enum -> string
            rd.setType(r.getType().name());       // enum -> string
            return rd;
        }).collect(Collectors.toList());

        // Map Activities
        List<TrackingDTO.ActivityDTO> actDtos = activities.stream().map(a -> {
            TrackingDTO.ActivityDTO ad = new TrackingDTO.ActivityDTO();
            ad.setActID(a.getActID());
            ad.setTitle(a.getTitle());
            ad.setDate(a.getDate());
            ad.setValidated(a.getValidated().name()); // enum -> string
            ad.setPoints(a.getPoints());
            ad.setActivityType(a.getActivityType());
            return ad;
        }).collect(Collectors.toList());

        dto.setRequests(reqDtos);
        dto.setActivities(actDtos);

        return dto;
    }
}
