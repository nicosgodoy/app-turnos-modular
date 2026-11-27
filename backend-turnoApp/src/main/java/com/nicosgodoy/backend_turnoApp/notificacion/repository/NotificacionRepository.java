package com.nicosgodoy.backend_turnoApp.notificacion.repository;

import com.nicosgodoy.backend_turnoApp.notificacion.entity.EstadoNotificacion;
import com.nicosgodoy.backend_turnoApp.notificacion.entity.Notificacion;
import com.nicosgodoy.backend_turnoApp.notificacion.entity.TipoNotificacion;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacionRepository extends JpaRepository<Notificacion, UUID> {

	boolean existsByTurnoIdAndTipoAndEstado(UUID turnoId, TipoNotificacion tipo, EstadoNotificacion estado);

	List<Notificacion> findByTurnoIdOrderByCreadoEnDesc(UUID turnoId);
}
