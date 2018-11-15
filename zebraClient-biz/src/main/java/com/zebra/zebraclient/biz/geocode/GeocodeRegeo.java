package com.zebra.zebraclient.biz.geocode;

import java.util.List;

import lombok.Data;

/****
 * 高德逆编码对象
 * 
 * @author owen
 *
 */
@Data
public class GeocodeRegeo {
    private String    status;
    private String    info;
    private String    infocode;
    private Regeocode regeocode;
}

@Data
class Regeocode {
    private String           formatted_address;
    private AddressComponent addressComponent;
}

@Data
class AddressComponent {
    private String             country;
    private String             province;
    private String             city;
    private String             citycode;
    private String             district;
    private String             adcode;
    private String             township;
    private String             towncode;
    private Neighborhood       neighborhood;
    private Building           building;
    private StreetNumber       streetNumber;
    private String             seaArea;
    private List<BusinessArea> businessAreas;
    private List<Road>         roads;
    private List<Roadinter>    roadinters;
    private List<Poi>          pois;
    private List<Aoi>          aois;
}

@Data
class Aoi {
    private String name;
    private String id;
    private String adcode;
    private String location;
    private String area;
}

@Data
class Poi {
    private String name;
    private String id;
    private String type;
    private String tel;
    private String distance;
    private String direction;
    private String address;
    private String location;
    private String businessarea;
}

@Data
class Roadinter {
    private String distance;
    private String direction;
    private String location;
    private String first_id;
    private String first_name;
    private String second_id;
    private String second_name;
}

@Data
class Road {
    private String name;
    private String id;
    private String distance;
    private String direction;
    private String location;
}

@Data
class BusinessArea {
    private String location;
    private String name;
    private String id;
}

@Data
class Neighborhood {
    private String name;
    private String type;
}

@Data
class Building {
    private String name;
    private String type;
}

@Data
class StreetNumber {
    private String street;
    private String number;
    private String location;
    private String direction;
    private String distance;
}
