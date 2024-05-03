// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: aaiextraction/dto/ocr.proto

package aaiextraction;

public interface OcrBlockOrBuilder extends
    // @@protoc_insertion_point(interface_extends:aaiextraction.OcrBlock)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string id = 1;</code>
   * @return The id.
   */
  java.lang.String getId();
  /**
   * <code>string id = 1;</code>
   * @return The bytes for id.
   */
  com.google.protobuf.ByteString
      getIdBytes();

  /**
   * <code>.aaiextraction.Geometry geometry = 2;</code>
   * @return Whether the geometry field is set.
   */
  boolean hasGeometry();
  /**
   * <code>.aaiextraction.Geometry geometry = 2;</code>
   * @return The geometry.
   */
  aaiextraction.Geometry getGeometry();
  /**
   * <code>.aaiextraction.Geometry geometry = 2;</code>
   */
  aaiextraction.GeometryOrBuilder getGeometryOrBuilder();

  /**
   * <code>string text = 3;</code>
   * @return The text.
   */
  java.lang.String getText();
  /**
   * <code>string text = 3;</code>
   * @return The bytes for text.
   */
  com.google.protobuf.ByteString
      getTextBytes();

  /**
   * <code>.aaiextraction.OcrBlockType block_type = 4;</code>
   * @return The enum numeric value on the wire for blockType.
   */
  int getBlockTypeValue();
  /**
   * <code>.aaiextraction.OcrBlockType block_type = 4;</code>
   * @return The blockType.
   */
  aaiextraction.OcrBlockType getBlockType();

  /**
   * <code>repeated string relationships = 5;</code>
   * @return A list containing the relationships.
   */
  java.util.List<java.lang.String>
      getRelationshipsList();
  /**
   * <code>repeated string relationships = 5;</code>
   * @return The count of relationships.
   */
  int getRelationshipsCount();
  /**
   * <code>repeated string relationships = 5;</code>
   * @param index The index of the element to return.
   * @return The relationships at the given index.
   */
  java.lang.String getRelationships(int index);
  /**
   * <code>repeated string relationships = 5;</code>
   * @param index The index of the value to return.
   * @return The bytes of the relationships at the given index.
   */
  com.google.protobuf.ByteString
      getRelationshipsBytes(int index);

  /**
   * <code>int32 page_num = 6;</code>
   * @return The pageNum.
   */
  int getPageNum();

  /**
   * <code>double confidence = 7;</code>
   * @return The confidence.
   */
  double getConfidence();
}