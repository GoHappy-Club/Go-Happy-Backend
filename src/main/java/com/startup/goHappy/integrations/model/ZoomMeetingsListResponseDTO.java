package com.startup.goHappy.integrations.model;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class ZoomMeetingsListResponseDTO implements Serializable {
    
    private static final long serialVersionUID = -218290644483495371L;

    private Integer page_size;

    private Integer page_number;

    private Integer page_count;

    public Integer total_records;

    public String next_page_token;

    public List<ZoomMeetingObjectDTO> meetings;
}
