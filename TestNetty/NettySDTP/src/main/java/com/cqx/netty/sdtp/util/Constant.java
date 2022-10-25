package com.cqx.netty.sdtp.util;

/**
 * SdtpUtil
 *
 * @author chenqixu
 */
public class Constant {
    public static final String LineENd = "\r\n";
    public static final String LoginID = "simple";
    public static final String sharedSecret = "123456";
    public static final String COMMON_RULE = "Length-L2-byte-F,LocalProvince-L2-byte-F,LocalCity-L2-byte-F,OwnerProvince-L2-byte-F,OwnerCity-L2-byte-F,RoamingType-L1-byte-F,Interface-L1-byte-F,ProbeID-L2-byte-F,XDRID-L16-hex-F,RAT-L1-byte-F,IMSI-L8-byte-F,PEI-L8-byte-F,MSISDN-L16-byte-F,";
    public static final String COMMON_NOLENGTH_RULE = "LocalProvince-L2-byte-F,LocalCity-L2-byte-F,OwnerProvince-L2-byte-F,OwnerCity-L2-byte-F,RoamingType-L1-byte-F,Interface-L1-byte-F,ProbeID-L2-byte-F,XDRID-L16-hex-F,RAT-L1-byte-F,IMSI-L8-byte-F,PEI-L8-byte-F,MSISDN-L16-byte-F,";
    public static final String N14_RULE = "ProcedureType-L1-byte-F,procedurestarttimeonForwardingGW-L8-byte-N0,procedureendtimeonForwardingGW-L8-byte-N0,procedurestarttimeonProbe-L8-byte-N0,procedureendtimeonProbe-L8-byte-N0,ProcedureStatus-L1-byte-F,httpreqtype-L1-byte-F,statuscode-L2-byte-F,FailureCause-L2-byte-F,SourceAMFAddress-LV-ip-F,DestinationAMFAddress-LV-ip-F,SourceAMFPort-L2-byte-F,DestinationAMFPort-L2-byte-F,Subproceduretype-L1-byte-F,RequestCause-L1-byte-F,USER_IPv4-T0L4-ip-F,USER_IPv6-T1L16-ip-F,TransferReason-T2L2-byte-F,pdusessionid-T3L1-byte-F,DNN-T4LV-string-F,TAC-T5L2-byte-F,CellID-T6L4-byte-F,accesstype-T7L1-byte-F,locationtype-T8L1-byte-F";
}
