package ua.deti.tqs.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.deti.tqs.utils.Constants;

@Slf4j
@RestController
@RequestMapping(Constants.API_V1 + "charging-stations")
@Tag(name = "Charging Stations", description = "The Charging Stations API")
@AllArgsConstructor
public class ChargingStationController {
}
