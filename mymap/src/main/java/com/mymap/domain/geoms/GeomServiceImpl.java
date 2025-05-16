package com.mymap.domain.geoms;

import com.mymap.domain.*;
import com.mymap.domain.clusters.dto.JourneyDTO;
import com.mymap.domain.clusters.dto.MarkerClusterDTO;
import com.mymap.domain.clusters.entity.Journey;
import com.mymap.domain.clusters.repository.JourneyRepository;
import com.mymap.exception.BusinessException;
import com.mymap.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Marker;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GeomServiceImpl implements GeomService{
    private final FromToGeomRepository fromToGeomRepository;
    private final BusRepository busRepository;
    private final SubwayRepository subwayRepository;
    private final BikeRepository bikeRepository;
    private final JourneyRepository journeyRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Override
    public List<MarkerDTO> findGeoms(List<MarkerClusterDTO> clusters, Long auth, long jno) {
        Journey journey = journeyRepository.findByNo(jno)
                .orElseThrow(()->new BusinessException(ErrorCode.NOT_EXIST));
        List<MarkerDTO> markers = new ArrayList<>();
        for(MarkerClusterDTO dto : clusters){
            MarkerDTO mdto = new MarkerDTO();
            mdto.setClusterName(dto.getClusterName());
            if("from_to_geo".equals(dto.getGeomTable())){
                FromToGeom fromToGeom = fromToGeomRepository.findByUserNoAndName(auth,dto.getClusterName())
                        .orElseThrow(()->new BusinessException(ErrorCode.NOT_EXIST));
                if(dto.getClusterName().equals(journey.getFromName()))
                    mdto.setGroup("dp");
                else
                    mdto.setGroup("ar");
                mdto.setLon(String.valueOf(fromToGeom.getGeom().getX()));
                mdto.setLat(String.valueOf(fromToGeom.getGeom().getY()));
            } else if("subway".equals(dto.getGeomTable())){
                Pageable page = PageRequest.of(0, 1, Sort.by("no").ascending());
                Subway subs = subwayRepository.findByStName(dto.getClusterName(),page)
                        .getContent().stream().findFirst()
                        .orElseThrow(()->new BusinessException(ErrorCode.NOT_EXIST));
                mdto.setGroup("tf");
                mdto.setLon(String.valueOf(subs.getGeom().getX()));
                mdto.setLat(String.valueOf(subs.getGeom().getY()));
                mdto.setStid(subs.getSubwayId());
            } else if("bus".equals(dto.getGeomTable()) || "buses".equals(dto.getGeomTable())){
                Bus bus = busRepository.findByArsId(dto.getClusterBus()[0])
                        .orElseThrow(()->new BusinessException(ErrorCode.NOT_EXIST));
                mdto.setGroup("tf");
                mdto.setLon(String.valueOf(bus.getGeom().getX()));
                mdto.setLat(String.valueOf(bus.getGeom().getY()));
                mdto.setStid(bus.getArsId());
            } else if("bike".equals(dto.getGeomTable()) || "bikes".equals(dto.getGeomTable())){
                Bike bike = bikeRepository.findByStId(dto.getClusterBike()[0])
                        .orElseThrow(()->new BusinessException(ErrorCode.NOT_EXIST));
                mdto.setGroup("tf");
                mdto.setLon(String.valueOf(bike.getGeom().getX()));
                mdto.setLat(String.valueOf(bike.getGeom().getY()));
                mdto.setStid(bike.getStationId());
            } else
                throw new BusinessException(ErrorCode.NOT_EXIST);
            markers.add(mdto);
        }
        return markers;
    }

    @Override
    @Transactional
    public void createFromToGeoms(JourneyDTO journey) {
        FromToGeom fromGeom = fromToGeomRepository.findByUserNoAndName(journey.getUserNo(), journey.getFromName()).orElse(null);
        FromToGeom toGeom = fromToGeomRepository.findByUserNoAndName(journey.getUserNo(), journey.getToName()).orElse(null);
        if(fromGeom==null){
            Point fromPoint = geometryFactory.createPoint(new Coordinate(journey.getFromGeoms()[0],journey.getFromGeoms()[1]));
            FromToGeom entity = FromToGeom.builder()
                            .userNo(journey.getUserNo()).name(journey.getFromName()).geom(fromPoint)
                            .build();
            fromToGeomRepository.save(entity);
        }
        if(toGeom==null){
            Point toPoint = geometryFactory.createPoint(new Coordinate(journey.getToGeoms()[0],journey.getToGeoms()[1]));
            FromToGeom entity = FromToGeom.builder()
                    .userNo(journey.getUserNo()).name(journey.getToName()).geom(toPoint)
                    .build();
            fromToGeomRepository.save(entity);
        }
    }

    @Override
    @Transactional
    public void deleteFromToGeoms(JourneyDTO dto) {
        int fromCount = journeyRepository.findByUserNoAndFromName(dto.getUserNo(),dto.getFromName());
        int toCount = journeyRepository.findByUserNoAndToName(dto.getUserNo(),dto.getToName());
        if(fromCount==0)
            fromToGeomRepository.deleteByUserNoAndName(dto.getUserNo(),dto.getFromName());
        if(toCount==0)
            fromToGeomRepository.deleteByUserNoAndName(dto.getUserNo(),dto.getToName());
    }
}
