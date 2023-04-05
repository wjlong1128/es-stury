package com.wjl.hotel3.mapper;


import com.wjl.hotel3.model.pojo.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface HotelMapper extends JpaRepository<Hotel,Long> {

    // hql
     @Query("select h from Hotel h where h.name = :s")
    Hotel findOneByName(@Param("s") String name);

}
