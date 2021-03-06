package com.emse.faircorp.controller;

import com.emse.faircorp.dao.LightDao;
import com.emse.faircorp.dao.RoomDao;
import com.emse.faircorp.dto.LightDto;
import com.emse.faircorp.model.Light;
import com.emse.faircorp.model.Room;
import com.emse.faircorp.model.Status;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/api/lights")
@Transactional
public class LightController {
    public void addHeaders(HttpServletResponse response) {
        response.addHeader("access-control-allow-credentials", "true");
        response.addHeader("access-control-allow-headers", "Origin,Accept,X-Requested-With,Content-Type,X-Auth-Token,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization");
        response.addHeader("access-control-allow-origin", "*");
        response.addHeader("content-type", "application/json;charset=UTF-8");
    }

    @Autowired
    private LightDao lightDao;

    @Autowired
    private RoomDao roomDao;

    @GetMapping
    public List<LightDto> findAll() {
        return lightDao.findAll()
                .stream()
                .map(LightDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping(path = "/{id}")
    public LightDto findById(@PathVariable Long id) {
        return lightDao.findById(id).
                map(light -> new LightDto(light)).
                orElse(null);
    }

    @RequestMapping(value = "/{id}/switch_get_room_lights", method = RequestMethod.PUT)
    public List<LightDto> switchStatus(@PathVariable Long id) throws MqttException {
        Light light1 = lightDao.findById(id).orElseThrow(IllegalArgumentException::new);
        light1.setStatus(light1.getStatus() == Status.ON ? Status.OFF : Status.ON);

        Room room = light1.getRoom();
        Long roomId = room.getId();

        MqttClient publisher = new MqttClient("tcp://max.isasecret.com:1723", "1");
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName("majinfo2019");
        options.setPassword("Y@_oK2".toCharArray());

        MqttMessage msg = new MqttMessage();
        if (light1.getStatus() == Status.ON) {
            msg.setPayload(("{" + roomId + ":ON}").getBytes());
        } else {
            msg.setPayload(("{" + roomId + ":OFF}").getBytes());
        }

        publisher.connect(options);
        if (publisher.isConnected()) {
            publisher.publish("switchLight", msg);
        }
        return roomDao.findRoomLightsByRoomId(roomId).stream().map(light -> new LightDto(light)).collect(Collectors.toList());
    }

    @PostMapping
    public LightDto create(@RequestBody LightDto dto) {
        Light light = null;
        if (dto.getId() != null) {
            light = lightDao.findById(dto.getId()).orElse(null);
        }
        if (light == null) {
            light = lightDao.save(new Light(dto.getLevel(), dto.getStatus(), roomDao.getOne(dto.getRoomId())));
        } else {
            light.setLevel(dto.getLevel());
            light.setStatus(dto.getStatus());
            lightDao.save(light);
        }
        return new LightDto(light);
    }

    @DeleteMapping(path = "/{id}")
    public void delete(@PathVariable Long id) {
        lightDao.deleteById(id);
    }
}
