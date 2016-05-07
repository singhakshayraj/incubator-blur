package org.apache.blur.mapreduce.lib.update;

public enum BlurIndexCounter {

  NEW_RECORDS,
  ROW_IDS_FROM_INDEX,
  ROW_IDS_TO_UPDATE_FROM_NEW_DATA,
  ROW_IDS_FROM_NEW_DATA,
  
  INPUT_FORMAT_MAPPER, 
  INPUT_FORMAT_EXISTING_RECORDS,
  
  LOOKUP_MAPPER, 
  LOOKUP_MAPPER_EXISTING_RECORDS, 
  LOOKUP_MAPPER_ROW_LOOKUP_ATTEMPT

}