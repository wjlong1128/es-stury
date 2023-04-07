package cn.itcast.hotel.model.pojo;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class HotelDoc {
    private Long id;
    private String name;
    private String address;
    private Integer price;
    private Integer score;
    private String brand;
    private String city;
    private String starName;
    private String business;
    private String location;
    private String pic;
    private Object distance;
    private Boolean isAD;
    // 对应es的自动补全
    private List<String> suggestion;

    public HotelDoc(Hotel hotel) {
        this.id = hotel.getId();
        this.name = hotel.getName();
        this.address = hotel.getAddress();
        this.price = hotel.getPrice();
        this.score = hotel.getScore();
        this.brand = hotel.getBrand();
        this.city = hotel.getCity();
        this.starName = hotel.getStarName();
        this.business = hotel.getBusiness();
        this.location = hotel.getLatitude() + ", " + hotel.getLongitude();
        this.pic = hotel.getPic();
        this.suggestion = new ArrayList<>();
        if (!StringUtils.isBlank(business)){
            this.suggestion.add(brand);
            if (business.contains("/")){
                for (String s : business.split("/")) {
                    this.suggestion.add(s);
                }
            }else{
                this.suggestion.add(business);
            }
        }
    }
}
