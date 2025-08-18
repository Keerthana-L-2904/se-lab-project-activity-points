package com.example.student_activity_points.repository;

import com.example.student_activity_points.domain.Notification;
import org.springframework.data.repository.CrudRepository;

public interface NotificationRepository extends CrudRepository<Notification, Long> {
}
