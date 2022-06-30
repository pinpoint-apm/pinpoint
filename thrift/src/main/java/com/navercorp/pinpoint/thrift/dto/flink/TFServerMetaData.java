/**
 * Autogenerated by Thrift Compiler (0.16.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.navercorp.pinpoint.thrift.dto.flink;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.16.0)", date = "2022-06-30")
public class TFServerMetaData implements org.apache.thrift.TBase<TFServerMetaData, TFServerMetaData._Fields>, java.io.Serializable, Cloneable, Comparable<TFServerMetaData> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TFServerMetaData");

  private static final org.apache.thrift.protocol.TField SERVER_INFO_FIELD_DESC = new org.apache.thrift.protocol.TField("serverInfo", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField VM_ARGS_FIELD_DESC = new org.apache.thrift.protocol.TField("vmArgs", org.apache.thrift.protocol.TType.LIST, (short)2);
  private static final org.apache.thrift.protocol.TField SERVICE_INFOS_FIELD_DESC = new org.apache.thrift.protocol.TField("serviceInfos", org.apache.thrift.protocol.TType.LIST, (short)10);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new TFServerMetaDataStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new TFServerMetaDataTupleSchemeFactory();

  private @org.apache.thrift.annotation.Nullable java.lang.String serverInfo; // optional
  private @org.apache.thrift.annotation.Nullable java.util.List<java.lang.String> vmArgs; // optional
  private @org.apache.thrift.annotation.Nullable java.util.List<TFServiceInfo> serviceInfos; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    SERVER_INFO((short)1, "serverInfo"),
    VM_ARGS((short)2, "vmArgs"),
    SERVICE_INFOS((short)10, "serviceInfos");

    private static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();

    static {
      for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    @org.apache.thrift.annotation.Nullable
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // SERVER_INFO
          return SERVER_INFO;
        case 2: // VM_ARGS
          return VM_ARGS;
        case 10: // SERVICE_INFOS
          return SERVICE_INFOS;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new java.lang.IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    @org.apache.thrift.annotation.Nullable
    public static _Fields findByName(java.lang.String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final java.lang.String _fieldName;

    _Fields(short thriftId, java.lang.String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public java.lang.String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final _Fields optionals[] = {_Fields.SERVER_INFO,_Fields.VM_ARGS,_Fields.SERVICE_INFOS};
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.SERVER_INFO, new org.apache.thrift.meta_data.FieldMetaData("serverInfo", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.VM_ARGS, new org.apache.thrift.meta_data.FieldMetaData("vmArgs", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    tmpMap.put(_Fields.SERVICE_INFOS, new org.apache.thrift.meta_data.FieldMetaData("serviceInfos", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TFServiceInfo.class))));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TFServerMetaData.class, metaDataMap);
  }

  public TFServerMetaData() {
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public TFServerMetaData(TFServerMetaData other) {
    if (other.isSetServerInfo()) {
      this.serverInfo = other.serverInfo;
    }
    if (other.isSetVmArgs()) {
      java.util.List<java.lang.String> __this__vmArgs = new java.util.ArrayList<java.lang.String>(other.vmArgs);
      this.vmArgs = __this__vmArgs;
    }
    if (other.isSetServiceInfos()) {
      java.util.List<TFServiceInfo> __this__serviceInfos = new java.util.ArrayList<TFServiceInfo>(other.serviceInfos.size());
      for (TFServiceInfo other_element : other.serviceInfos) {
        __this__serviceInfos.add(new TFServiceInfo(other_element));
      }
      this.serviceInfos = __this__serviceInfos;
    }
  }

  public TFServerMetaData deepCopy() {
    return new TFServerMetaData(this);
  }

  @Override
  public void clear() {
    this.serverInfo = null;
    this.vmArgs = null;
    this.serviceInfos = null;
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.String getServerInfo() {
    return this.serverInfo;
  }

  public void setServerInfo(@org.apache.thrift.annotation.Nullable java.lang.String serverInfo) {
    this.serverInfo = serverInfo;
  }

  public void unsetServerInfo() {
    this.serverInfo = null;
  }

  /** Returns true if field serverInfo is set (has been assigned a value) and false otherwise */
  public boolean isSetServerInfo() {
    return this.serverInfo != null;
  }

  public void setServerInfoIsSet(boolean value) {
    if (!value) {
      this.serverInfo = null;
    }
  }

  public int getVmArgsSize() {
    return (this.vmArgs == null) ? 0 : this.vmArgs.size();
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.Iterator<java.lang.String> getVmArgsIterator() {
    return (this.vmArgs == null) ? null : this.vmArgs.iterator();
  }

  public void addToVmArgs(java.lang.String elem) {
    if (this.vmArgs == null) {
      this.vmArgs = new java.util.ArrayList<java.lang.String>();
    }
    this.vmArgs.add(elem);
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.List<java.lang.String> getVmArgs() {
    return this.vmArgs;
  }

  public void setVmArgs(@org.apache.thrift.annotation.Nullable java.util.List<java.lang.String> vmArgs) {
    this.vmArgs = vmArgs;
  }

  public void unsetVmArgs() {
    this.vmArgs = null;
  }

  /** Returns true if field vmArgs is set (has been assigned a value) and false otherwise */
  public boolean isSetVmArgs() {
    return this.vmArgs != null;
  }

  public void setVmArgsIsSet(boolean value) {
    if (!value) {
      this.vmArgs = null;
    }
  }

  public int getServiceInfosSize() {
    return (this.serviceInfos == null) ? 0 : this.serviceInfos.size();
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.Iterator<TFServiceInfo> getServiceInfosIterator() {
    return (this.serviceInfos == null) ? null : this.serviceInfos.iterator();
  }

  public void addToServiceInfos(TFServiceInfo elem) {
    if (this.serviceInfos == null) {
      this.serviceInfos = new java.util.ArrayList<TFServiceInfo>();
    }
    this.serviceInfos.add(elem);
  }

  @org.apache.thrift.annotation.Nullable
  public java.util.List<TFServiceInfo> getServiceInfos() {
    return this.serviceInfos;
  }

  public void setServiceInfos(@org.apache.thrift.annotation.Nullable java.util.List<TFServiceInfo> serviceInfos) {
    this.serviceInfos = serviceInfos;
  }

  public void unsetServiceInfos() {
    this.serviceInfos = null;
  }

  /** Returns true if field serviceInfos is set (has been assigned a value) and false otherwise */
  public boolean isSetServiceInfos() {
    return this.serviceInfos != null;
  }

  public void setServiceInfosIsSet(boolean value) {
    if (!value) {
      this.serviceInfos = null;
    }
  }

  public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
    switch (field) {
    case SERVER_INFO:
      if (value == null) {
        unsetServerInfo();
      } else {
        setServerInfo((java.lang.String)value);
      }
      break;

    case VM_ARGS:
      if (value == null) {
        unsetVmArgs();
      } else {
        setVmArgs((java.util.List<java.lang.String>)value);
      }
      break;

    case SERVICE_INFOS:
      if (value == null) {
        unsetServiceInfos();
      } else {
        setServiceInfos((java.util.List<TFServiceInfo>)value);
      }
      break;

    }
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case SERVER_INFO:
      return getServerInfo();

    case VM_ARGS:
      return getVmArgs();

    case SERVICE_INFOS:
      return getServiceInfos();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case SERVER_INFO:
      return isSetServerInfo();
    case VM_ARGS:
      return isSetVmArgs();
    case SERVICE_INFOS:
      return isSetServiceInfos();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that instanceof TFServerMetaData)
      return this.equals((TFServerMetaData)that);
    return false;
  }

  public boolean equals(TFServerMetaData that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_serverInfo = true && this.isSetServerInfo();
    boolean that_present_serverInfo = true && that.isSetServerInfo();
    if (this_present_serverInfo || that_present_serverInfo) {
      if (!(this_present_serverInfo && that_present_serverInfo))
        return false;
      if (!this.serverInfo.equals(that.serverInfo))
        return false;
    }

    boolean this_present_vmArgs = true && this.isSetVmArgs();
    boolean that_present_vmArgs = true && that.isSetVmArgs();
    if (this_present_vmArgs || that_present_vmArgs) {
      if (!(this_present_vmArgs && that_present_vmArgs))
        return false;
      if (!this.vmArgs.equals(that.vmArgs))
        return false;
    }

    boolean this_present_serviceInfos = true && this.isSetServiceInfos();
    boolean that_present_serviceInfos = true && that.isSetServiceInfos();
    if (this_present_serviceInfos || that_present_serviceInfos) {
      if (!(this_present_serviceInfos && that_present_serviceInfos))
        return false;
      if (!this.serviceInfos.equals(that.serviceInfos))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((isSetServerInfo()) ? 131071 : 524287);
    if (isSetServerInfo())
      hashCode = hashCode * 8191 + serverInfo.hashCode();

    hashCode = hashCode * 8191 + ((isSetVmArgs()) ? 131071 : 524287);
    if (isSetVmArgs())
      hashCode = hashCode * 8191 + vmArgs.hashCode();

    hashCode = hashCode * 8191 + ((isSetServiceInfos()) ? 131071 : 524287);
    if (isSetServiceInfos())
      hashCode = hashCode * 8191 + serviceInfos.hashCode();

    return hashCode;
  }

  @Override
  public int compareTo(TFServerMetaData other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.compare(isSetServerInfo(), other.isSetServerInfo());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetServerInfo()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.serverInfo, other.serverInfo);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.compare(isSetVmArgs(), other.isSetVmArgs());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetVmArgs()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.vmArgs, other.vmArgs);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.compare(isSetServiceInfos(), other.isSetServiceInfos());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetServiceInfos()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.serviceInfos, other.serviceInfos);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  @org.apache.thrift.annotation.Nullable
  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    scheme(iprot).read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    scheme(oprot).write(oprot, this);
  }

  @Override
  public java.lang.String toString() {
    java.lang.StringBuilder sb = new java.lang.StringBuilder("TFServerMetaData(");
    boolean first = true;

    if (isSetServerInfo()) {
      sb.append("serverInfo:");
      if (this.serverInfo == null) {
        sb.append("null");
      } else {
        sb.append(this.serverInfo);
      }
      first = false;
    }
    if (isSetVmArgs()) {
      if (!first) sb.append(", ");
      sb.append("vmArgs:");
      if (this.vmArgs == null) {
        sb.append("null");
      } else {
        sb.append(this.vmArgs);
      }
      first = false;
    }
    if (isSetServiceInfos()) {
      if (!first) sb.append(", ");
      sb.append("serviceInfos:");
      if (this.serviceInfos == null) {
        sb.append("null");
      } else {
        sb.append(this.serviceInfos);
      }
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
    try {
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class TFServerMetaDataStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public TFServerMetaDataStandardScheme getScheme() {
      return new TFServerMetaDataStandardScheme();
    }
  }

  private static class TFServerMetaDataStandardScheme extends org.apache.thrift.scheme.StandardScheme<TFServerMetaData> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, TFServerMetaData struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // SERVER_INFO
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.serverInfo = iprot.readString();
              struct.setServerInfoIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // VM_ARGS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list8 = iprot.readListBegin();
                struct.vmArgs = new java.util.ArrayList<java.lang.String>(_list8.size);
                @org.apache.thrift.annotation.Nullable java.lang.String _elem9;
                for (int _i10 = 0; _i10 < _list8.size; ++_i10)
                {
                  _elem9 = iprot.readString();
                  struct.vmArgs.add(_elem9);
                }
                iprot.readListEnd();
              }
              struct.setVmArgsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 10: // SERVICE_INFOS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list11 = iprot.readListBegin();
                struct.serviceInfos = new java.util.ArrayList<TFServiceInfo>(_list11.size);
                @org.apache.thrift.annotation.Nullable TFServiceInfo _elem12;
                for (int _i13 = 0; _i13 < _list11.size; ++_i13)
                {
                  _elem12 = new TFServiceInfo();
                  _elem12.read(iprot);
                  struct.serviceInfos.add(_elem12);
                }
                iprot.readListEnd();
              }
              struct.setServiceInfosIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, TFServerMetaData struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.serverInfo != null) {
        if (struct.isSetServerInfo()) {
          oprot.writeFieldBegin(SERVER_INFO_FIELD_DESC);
          oprot.writeString(struct.serverInfo);
          oprot.writeFieldEnd();
        }
      }
      if (struct.vmArgs != null) {
        if (struct.isSetVmArgs()) {
          oprot.writeFieldBegin(VM_ARGS_FIELD_DESC);
          {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.vmArgs.size()));
            for (java.lang.String _iter14 : struct.vmArgs)
            {
              oprot.writeString(_iter14);
            }
            oprot.writeListEnd();
          }
          oprot.writeFieldEnd();
        }
      }
      if (struct.serviceInfos != null) {
        if (struct.isSetServiceInfos()) {
          oprot.writeFieldBegin(SERVICE_INFOS_FIELD_DESC);
          {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.serviceInfos.size()));
            for (TFServiceInfo _iter15 : struct.serviceInfos)
            {
              _iter15.write(oprot);
            }
            oprot.writeListEnd();
          }
          oprot.writeFieldEnd();
        }
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class TFServerMetaDataTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public TFServerMetaDataTupleScheme getScheme() {
      return new TFServerMetaDataTupleScheme();
    }
  }

  private static class TFServerMetaDataTupleScheme extends org.apache.thrift.scheme.TupleScheme<TFServerMetaData> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, TFServerMetaData struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetServerInfo()) {
        optionals.set(0);
      }
      if (struct.isSetVmArgs()) {
        optionals.set(1);
      }
      if (struct.isSetServiceInfos()) {
        optionals.set(2);
      }
      oprot.writeBitSet(optionals, 3);
      if (struct.isSetServerInfo()) {
        oprot.writeString(struct.serverInfo);
      }
      if (struct.isSetVmArgs()) {
        {
          oprot.writeI32(struct.vmArgs.size());
          for (java.lang.String _iter16 : struct.vmArgs)
          {
            oprot.writeString(_iter16);
          }
        }
      }
      if (struct.isSetServiceInfos()) {
        {
          oprot.writeI32(struct.serviceInfos.size());
          for (TFServiceInfo _iter17 : struct.serviceInfos)
          {
            _iter17.write(oprot);
          }
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, TFServerMetaData struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet incoming = iprot.readBitSet(3);
      if (incoming.get(0)) {
        struct.serverInfo = iprot.readString();
        struct.setServerInfoIsSet(true);
      }
      if (incoming.get(1)) {
        {
          org.apache.thrift.protocol.TList _list18 = iprot.readListBegin(org.apache.thrift.protocol.TType.STRING);
          struct.vmArgs = new java.util.ArrayList<java.lang.String>(_list18.size);
          @org.apache.thrift.annotation.Nullable java.lang.String _elem19;
          for (int _i20 = 0; _i20 < _list18.size; ++_i20)
          {
            _elem19 = iprot.readString();
            struct.vmArgs.add(_elem19);
          }
        }
        struct.setVmArgsIsSet(true);
      }
      if (incoming.get(2)) {
        {
          org.apache.thrift.protocol.TList _list21 = iprot.readListBegin(org.apache.thrift.protocol.TType.STRUCT);
          struct.serviceInfos = new java.util.ArrayList<TFServiceInfo>(_list21.size);
          @org.apache.thrift.annotation.Nullable TFServiceInfo _elem22;
          for (int _i23 = 0; _i23 < _list21.size; ++_i23)
          {
            _elem22 = new TFServiceInfo();
            _elem22.read(iprot);
            struct.serviceInfos.add(_elem22);
          }
        }
        struct.setServiceInfosIsSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

