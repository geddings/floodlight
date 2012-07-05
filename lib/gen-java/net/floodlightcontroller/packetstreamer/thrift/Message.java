/**
 * Autogenerated by Thrift Compiler (0.7.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
package net.floodlightcontroller.packetstreamer.thrift;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Message implements org.apache.thrift.TBase<Message, Message._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("Message");

  private static final org.apache.thrift.protocol.TField SESSION_IDS_FIELD_DESC = new org.apache.thrift.protocol.TField("sessionIDs", org.apache.thrift.protocol.TType.LIST, (short)1);
  private static final org.apache.thrift.protocol.TField PACKET_FIELD_DESC = new org.apache.thrift.protocol.TField("packet", org.apache.thrift.protocol.TType.STRUCT, (short)2);

  public List<String> sessionIDs; // required
  public Packet packet; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    SESSION_IDS((short)1, "sessionIDs"),
    PACKET((short)2, "packet");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // SESSION_IDS
          return SESSION_IDS;
        case 2: // PACKET
          return PACKET;
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
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments

  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.SESSION_IDS, new org.apache.thrift.meta_data.FieldMetaData("sessionIDs", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    tmpMap.put(_Fields.PACKET, new org.apache.thrift.meta_data.FieldMetaData("packet", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, Packet.class)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(Message.class, metaDataMap);
  }

  public Message() {
  }

  public Message(
    List<String> sessionIDs,
    Packet packet)
  {
    this();
    this.sessionIDs = sessionIDs;
    this.packet = packet;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public Message(Message other) {
    if (other.isSetSessionIDs()) {
      List<String> __this__sessionIDs = new ArrayList<String>();
      for (String other_element : other.sessionIDs) {
        __this__sessionIDs.add(other_element);
      }
      this.sessionIDs = __this__sessionIDs;
    }
    if (other.isSetPacket()) {
      this.packet = new Packet(other.packet);
    }
  }

  public Message deepCopy() {
    return new Message(this);
  }

  @Override
  public void clear() {
    this.sessionIDs = null;
    this.packet = null;
  }

  public int getSessionIDsSize() {
    return (this.sessionIDs == null) ? 0 : this.sessionIDs.size();
  }

  public java.util.Iterator<String> getSessionIDsIterator() {
    return (this.sessionIDs == null) ? null : this.sessionIDs.iterator();
  }

  public void addToSessionIDs(String elem) {
    if (this.sessionIDs == null) {
      this.sessionIDs = new ArrayList<String>();
    }
    this.sessionIDs.add(elem);
  }

  public List<String> getSessionIDs() {
    return this.sessionIDs;
  }

  public Message setSessionIDs(List<String> sessionIDs) {
    this.sessionIDs = sessionIDs;
    return this;
  }

  public void unsetSessionIDs() {
    this.sessionIDs = null;
  }

  /** Returns true if field sessionIDs is set (has been assigned a value) and false otherwise */
  public boolean isSetSessionIDs() {
    return this.sessionIDs != null;
  }

  public void setSessionIDsIsSet(boolean value) {
    if (!value) {
      this.sessionIDs = null;
    }
  }

  public Packet getPacket() {
    return this.packet;
  }

  public Message setPacket(Packet packet) {
    this.packet = packet;
    return this;
  }

  public void unsetPacket() {
    this.packet = null;
  }

  /** Returns true if field packet is set (has been assigned a value) and false otherwise */
  public boolean isSetPacket() {
    return this.packet != null;
  }

  public void setPacketIsSet(boolean value) {
    if (!value) {
      this.packet = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case SESSION_IDS:
      if (value == null) {
        unsetSessionIDs();
      } else {
        setSessionIDs((List<String>)value);
      }
      break;

    case PACKET:
      if (value == null) {
        unsetPacket();
      } else {
        setPacket((Packet)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case SESSION_IDS:
      return getSessionIDs();

    case PACKET:
      return getPacket();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case SESSION_IDS:
      return isSetSessionIDs();
    case PACKET:
      return isSetPacket();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof Message)
      return this.equals((Message)that);
    return false;
  }

  public boolean equals(Message that) {
    if (that == null)
      return false;

    boolean this_present_sessionIDs = true && this.isSetSessionIDs();
    boolean that_present_sessionIDs = true && that.isSetSessionIDs();
    if (this_present_sessionIDs || that_present_sessionIDs) {
      if (!(this_present_sessionIDs && that_present_sessionIDs))
        return false;
      if (!this.sessionIDs.equals(that.sessionIDs))
        return false;
    }

    boolean this_present_packet = true && this.isSetPacket();
    boolean that_present_packet = true && that.isSetPacket();
    if (this_present_packet || that_present_packet) {
      if (!(this_present_packet && that_present_packet))
        return false;
      if (!this.packet.equals(that.packet))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public int compareTo(Message other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    Message typedOther = (Message)other;

    lastComparison = Boolean.valueOf(isSetSessionIDs()).compareTo(typedOther.isSetSessionIDs());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSessionIDs()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sessionIDs, typedOther.sessionIDs);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetPacket()).compareTo(typedOther.isSetPacket());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetPacket()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.packet, typedOther.packet);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    org.apache.thrift.protocol.TField field;
    iprot.readStructBegin();
    while (true)
    {
      field = iprot.readFieldBegin();
      if (field.type == org.apache.thrift.protocol.TType.STOP) { 
        break;
      }
      switch (field.id) {
        case 1: // SESSION_IDS
          if (field.type == org.apache.thrift.protocol.TType.LIST) {
            {
              org.apache.thrift.protocol.TList _list0 = iprot.readListBegin();
              this.sessionIDs = new ArrayList<String>(_list0.size);
              for (int _i1 = 0; _i1 < _list0.size; ++_i1)
              {
                String _elem2; // required
                _elem2 = iprot.readString();
                this.sessionIDs.add(_elem2);
              }
              iprot.readListEnd();
            }
          } else { 
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 2: // PACKET
          if (field.type == org.apache.thrift.protocol.TType.STRUCT) {
            this.packet = new Packet();
            this.packet.read(iprot);
          } else { 
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
          }
          break;
        default:
          org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
      }
      iprot.readFieldEnd();
    }
    iprot.readStructEnd();

    // check for required fields of primitive type, which can't be checked in the validate method
    validate();
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    validate();

    oprot.writeStructBegin(STRUCT_DESC);
    if (this.sessionIDs != null) {
      oprot.writeFieldBegin(SESSION_IDS_FIELD_DESC);
      {
        oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, this.sessionIDs.size()));
        for (String _iter3 : this.sessionIDs)
        {
          oprot.writeString(_iter3);
        }
        oprot.writeListEnd();
      }
      oprot.writeFieldEnd();
    }
    if (this.packet != null) {
      oprot.writeFieldBegin(PACKET_FIELD_DESC);
      this.packet.write(oprot);
      oprot.writeFieldEnd();
    }
    oprot.writeFieldStop();
    oprot.writeStructEnd();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Message(");
    boolean first = true;

    sb.append("sessionIDs:");
    if (this.sessionIDs == null) {
      sb.append("null");
    } else {
      sb.append(this.sessionIDs);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("packet:");
    if (this.packet == null) {
      sb.append("null");
    } else {
      sb.append(this.packet);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

}

