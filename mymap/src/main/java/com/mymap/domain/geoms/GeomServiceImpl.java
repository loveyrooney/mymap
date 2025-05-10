package com.mymap.domain.geoms;

import com.mymap.domain.*;
import com.mymap.domain.clusters.dto.JourneyDTO;
import com.mymap.domain.clusters.dto.MarkerClusterDTO;
import com.mymap.exception.BusinessException;
import com.mymap.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Override
    public List<MarkerDTO> findGeoms(List<MarkerClusterDTO> clusters, Long auth) {
        List<MarkerDTO> markers = new ArrayList<>();
        for(MarkerClusterDTO dto : clusters){
            MarkerDTO m;
            if("from_to_geo".equals(dto.getGeomTable())){
                m = fromToGeomRepository.findByUserNoAndName(auth,dto.getClusterName())
                        .orElseThrow(()->new BusinessException(ErrorCode.NOT_EXIST));
            } else if("subway".equals(dto.getGeomTable())){
                Pageable page = PageRequest.of(0, 1, Sort.by("no").ascending());
                Object[] subs = subwayRepository.findByStName(dto.getClusterName(),page)
                        .getContent().stream().findFirst()
                        .orElseThrow(()->new BusinessException(ErrorCode.NOT_EXIST));
                m = MarkerDTO.builder()
                        .clusterName((String)subs[0])
                        .stName((String)subs[1])
                        .geom((String)subs[2].toString())
                        .build();
            } else if("bus".equals(dto.getGeomTable()) || "buses".equals(dto.getGeomTable())){
                m = busRepository.findByArsId(dto.getClusterBus()[0])
                        .orElseThrow(()->new BusinessException(ErrorCode.NOT_EXIST));
                m.setClusterName(dto.getClusterName());
            } else if("bike".equals(dto.getGeomTable()) || "bikes".equals(dto.getGeomTable())){
                m = bikeRepository.findByStId(dto.getClusterBike()[0])
                        .orElseThrow(()->new BusinessException(ErrorCode.NOT_EXIST));
                m.setClusterName(dto.getClusterName());
            } else
                throw new BusinessException(ErrorCode.NOT_EXIST);
            markers.add(m);
        }
        return markers;
    }

    @Override
    @Transactional
    public void createFromToGeoms(JourneyDTO journey) {
        MarkerDTO fromGeom = fromToGeomRepository.findByUserNoAndName(journey.getUserNo(), journey.getFromName()).orElse(null);
        MarkerDTO toGeom = fromToGeomRepository.findByUserNoAndName(journey.getUserNo(), journey.getToName()).orElse(null);
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
        fromToGeomRepository.deleteByUserNoAndName(dto.getUserNo(),dto.getFromName());
        fromToGeomRepository.deleteByUserNoAndName(dto.getUserNo(),dto.getToName());
    }
}
