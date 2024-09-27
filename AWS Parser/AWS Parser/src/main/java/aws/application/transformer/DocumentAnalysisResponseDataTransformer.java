/* Copyright (c) 2023 Automation Anywhere. All rights reserved.
 *
 * This software is the proprietary information of Automation Anywhere. You shall use it only in
 * accordance with the terms of the license agreement you entered into with Automation Anywhere.
 */
package aws.application.transformer;

import aaiextraction.DataType;
import aaiextraction.DocDetectMetadata;
import aaiextraction.DocDetectResult;
import aaiextraction.EngineData;
import aaiextraction.EngineMetadata;
import aaiextraction.ExecutionStatus;
import aaiextraction.ExtractionMetadata;
import aaiextraction.ExtractionPage;
import aaiextraction.ExtractionResult;
import aaiextraction.FeatureValue;
import aaiextraction.Geometry;
import aaiextraction.ImagePreprocessingMetadata;
import aaiextraction.ImagePreprocessingPage;
import aaiextraction.ImagePreprocessingResult;
import aaiextraction.KeyValueFeature;
import aaiextraction.OcrBlock;
import aaiextraction.OcrBlockType;
import aaiextraction.OcrMetadata;
import aaiextraction.OcrMetadataPage;
import aaiextraction.OcrResult;
import aaiextraction.TableCellFeature;
import aaiextraction.TableFeature;
import aaiextraction.TableHeaderFeature;
import aaiextraction.TableRowFeature;
import aws.application.model.Domain;
import aws.application.model.Field;
import aws.application.service.splitter.DocumentSplitter;
import aws.application.service.splitter.DocumentSplitterFactory;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import org.apache.http.HttpStatus;
import software.amazon.awssdk.services.textract.model.Block;
import software.amazon.awssdk.services.textract.model.BlockType;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextResponse;
import software.amazon.awssdk.services.textract.model.EntityType;
import software.amazon.awssdk.services.textract.model.GetDocumentAnalysisResponse;
import software.amazon.awssdk.services.textract.model.Point;
import software.amazon.awssdk.services.textract.model.Relationship;
import software.amazon.awssdk.services.textract.model.RelationshipType;

public class DocumentAnalysisResponseDataTransformer {

    public EngineData transform(DetectDocumentTextResponse detectDocumentTextResponse) {

        List<Block> docInfo = detectDocumentTextResponse.blocks();
        Iterator<Block> blockIterator = docInfo.iterator();

        while (blockIterator.hasNext()) {
            Block block = blockIterator.next();
            System.out.println("The block type is " + block.blockType().toString());
        }

        // TODO : transformation
        EngineData engineData = EngineData.newBuilder().build();

        return engineData;
    }

    public EngineData transform(
            GetDocumentAnalysisResponse analyzeDocument, String inputImageFilePath, Domain domain) {

        ImagePreprocessingResult imagePreprocessingResult =
                toImagePreprocessingResult(analyzeDocument, inputImageFilePath);
        EngineData engineData =
                EngineData.newBuilder()
                        .setMetadata(toEngineMetadata(analyzeDocument, inputImageFilePath))
                        .setImagePreprocessingResult(imagePreprocessingResult)
                        .setOcrResult(toOcrResult(analyzeDocument, imagePreprocessingResult))
                        .setDocDetectResult(toDocDetectResult(analyzeDocument))
                        .setExtractionResult(
                                toExtractionResult(
                                        analyzeDocument, imagePreprocessingResult, domain))
                        .build();
        return engineData;
    }

    private List<Block> getBlocks(
            Map<BlocksById, Block> blockIdMap, Block block, RelationshipType relationshipType) {

        Optional<Relationship> relationshipOptional =
                block.relationships().stream()
                        .filter(relationship -> relationship.type() == relationshipType)
                        .findFirst();

        if (relationshipOptional.isEmpty()) {
            return null;
        }

        List<String> blockIds = relationshipOptional.get().ids();

        List<Block> blocks =
                blockIds.stream()
                        .map(blockId -> blockIdMap.get(new BlocksById(blockId)))
                        .collect(Collectors.toList());
        return blocks;
    }

    private Map<BlocksByType, BlocksByType> organiseBlocks(List<Block> blocks) {
        Map<BlocksByType, BlocksByType> blocksByTypeMap = new HashMap<>();
        for (Block block : blocks) {

            BlocksByType blocksByType = new BlocksByType(block.blockType());
            if (blocksByTypeMap.get(blocksByType) == null) {
                blocksByTypeMap.put(blocksByType, blocksByType);
            }
            blocksByType = blocksByTypeMap.get(blocksByType);

            Map<BlocksById, Block> blocksByIdMap = blocksByType.getBlockIdMap();
            BlocksById blocksById = new BlocksById(block.id(), block);
            blocksByIdMap.put(blocksById, block);
        }
        return blocksByTypeMap;
    }

    private ImagePreprocessingResult toImagePreprocessingResult(
            GetDocumentAnalysisResponse analyzeDocument, String inputImageFilePath) {

        List<String> imagePaths = getImagePaths(inputImageFilePath);

        ImagePreprocessingResult.Builder builder =
                ImagePreprocessingResult.newBuilder()
                        .setMetadata(
                                ImagePreprocessingMetadata.newBuilder()
                                        .setFilepath(inputImageFilePath)
                                        .setExecutionStatus(
                                                ExecutionStatus.newBuilder()
                                                        .setStatusCode(200)
                                                        .setStatusMessage("SUCCESS")
                                                        .setMessage(
                                                                "Document processed successfully")
                                                        .build())
                                        .build());

        for (String imagePath : imagePaths) {
            ImagePreprocessingPage imagePreprocessingPage = toImagePreprocessingPage(imagePath);
            builder.addPages(imagePreprocessingPage);
        }
        return builder.build();
    }

    private OcrMetadataPage toOcrMetadataPage(
            ImagePreprocessingPage imagePreprocessingPage, int pageNum) {
        return OcrMetadataPage.newBuilder()
                .setFilepath(imagePreprocessingPage.getFilepath())
                .setPageNum(pageNum)
                .build();
    }

    private ImagePreprocessingPage toImagePreprocessingPage(String inputImageFilePath) {

        BufferedImage readImage = null;

        try {
            readImage = ImageIO.read(new File(inputImageFilePath));
            int height = readImage.getHeight();
            int width = readImage.getWidth();

            return ImagePreprocessingPage.newBuilder()
                    .setFilepath(inputImageFilePath)
                    .setWidth(width)
                    .setHeight(height)
                    .build();

        } catch (Exception e) {
            readImage = null;
            throw new RuntimeException(e);
        }
    }

    private ExtractionPage toExtractionPage(
            ImagePreprocessingPage imagePreprocessingPage, int pageNum) {
        return ExtractionPage.newBuilder()
                .setFilepath(imagePreprocessingPage.getFilepath())
                .setPageNum(pageNum)
                .setWidth(imagePreprocessingPage.getWidth())
                .setHeight(imagePreprocessingPage.getHeight())
                .build();
    }

    private OcrResult toOcrResult(
            GetDocumentAnalysisResponse analyzeDocument,
            ImagePreprocessingResult imagePreprocessingResult) {

        OcrMetadata.Builder ocrMetadataBuilder =
                OcrMetadata.newBuilder()
                        .setExecutionStatus(
                                ExecutionStatus.newBuilder()
                                        .setStatusCode(HttpStatus.SC_OK)
                                        .setStatusMessage("SUCCESS")
                                        .setMessage("Document processed successfully")
                                        .build())
                        .setNumberOfPages(imagePreprocessingResult.getPagesCount());

        for (int i = 0; i < imagePreprocessingResult.getPagesList().size(); i++) {
            ImagePreprocessingPage imagePreprocessingPage = imagePreprocessingResult.getPages(i);
            ocrMetadataBuilder.addPages(toOcrMetadataPage(imagePreprocessingPage, i));
        }

        OcrResult.Builder ocrResultBuilder =
                OcrResult.newBuilder().setMetadata(ocrMetadataBuilder.build());

        analyzeDocument.blocks().stream()
                .filter(
                        block ->
                                block.blockType() == BlockType.WORD
                                        || block.blockType() == BlockType.LINE
                                        || block.blockType() == BlockType.SIGNATURE)
                .forEach(
                        block -> {
                            ocrResultBuilder.addBlocks(toOCRBlock(block, imagePreprocessingResult));
                        });

        return ocrResultBuilder.build();
    }

    private ExtractionResult toExtractionResult(
            GetDocumentAnalysisResponse analyzeDocument,
            ImagePreprocessingResult imagePreprocessingResult,
            Domain domain) {

        ExtractionResult.Builder extractionResultBuilder = ExtractionResult.newBuilder();

        ExtractionMetadata.Builder builder =
                ExtractionMetadata.newBuilder()
                        .setFilepath(imagePreprocessingResult.getMetadata().getFilepath())
                        .setExecutionStatus(
                                ExecutionStatus.newBuilder()
                                        .setStatusCode(HttpStatus.SC_OK)
                                        .setStatusMessage("SUCCESS")
                                        .setMessage("Document processed successfully")
                                        .build());

        for (int i = 0; i < imagePreprocessingResult.getPagesList().size(); i++) {
            ImagePreprocessingPage imagePreprocessingPage = imagePreprocessingResult.getPages(i);
            builder.addPages(toExtractionPage(imagePreprocessingPage, i));
        }
        extractionResultBuilder.setMetadata(builder.build());

        List<Block> blocks = analyzeDocument.blocks();
        Map<BlocksByType, BlocksByType> blocksByTypeMap = organiseBlocks(blocks);
        toKeyValuePairs(blocksByTypeMap, extractionResultBuilder, imagePreprocessingResult, domain);
        toTables(blocksByTypeMap, extractionResultBuilder, imagePreprocessingResult, domain);
        toGraphicFeatures(blocksByTypeMap, extractionResultBuilder, imagePreprocessingResult);

        return extractionResultBuilder.build();
    }

    private void toKeyValuePairs(
            Map<BlocksByType, BlocksByType> blocksByTypeMap,
            ExtractionResult.Builder extractionResultBuilder,
            ImagePreprocessingResult imagePreprocessingResult,
            Domain domain) {

        BlocksByType keyValueBlocks =
                blocksByTypeMap.get(new BlocksByType(BlockType.KEY_VALUE_SET));
        Map<BlocksById, Block> keyValueBlockIdMap = keyValueBlocks.getBlockIdMap();

        BlocksByType wordBlocks = blocksByTypeMap.get(new BlocksByType(BlockType.WORD));
        Map<BlocksById, Block> wordBlockMap = wordBlocks.getBlockIdMap();

        Map<BlocksById, Block> filteredBlockIdMap =
                keyValueBlocks.getBlockIdMap().entrySet().stream()
                        .filter(
                                blocksByIdBlockEntry -> {
                                    return blocksByIdBlockEntry.getValue().entityTypes().get(0)
                                            == EntityType.KEY;
                                })
                        .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));

        for (Map.Entry<BlocksById, Block> blockIdEntry : filteredBlockIdMap.entrySet()) {
            Block keyBlock = blockIdEntry.getValue();

            List<Block> keyBlocks = getBlocks(wordBlockMap, keyBlock, RelationshipType.CHILD);
            List<String> keyList = keyBlocks.stream().map(Block::text).collect(Collectors.toList());
            String keyText = String.join(" ", keyList);

            String fieldName = findKeyByAlias(keyText, domain.getFields());
            if (fieldName == null || fieldName.isEmpty()) {
                continue;
            }

            Block valueBlock =
                    getBlocks(keyValueBlockIdMap, keyBlock, RelationshipType.VALUE).stream()
                            .findFirst()
                            .get();

            List<Block> valueBlocks = getBlocks(wordBlockMap, valueBlock, RelationshipType.CHILD);
            List<String> valueList =
                    valueBlocks.stream().map(Block::text).collect(Collectors.toList());

            String valueText = String.join(" ", valueList);

            int pageNumber = getPageNumber(keyBlock);

            extractionResultBuilder.addKeyValueFeatures(
                    KeyValueFeature.newBuilder()
                            .setId(keyBlock.id())
                            .setDomainFieldKey(fieldName)
                            .setGeometry(toGeometry(keyBlock, imagePreprocessingResult))
                            .setText(fieldName)
                            .setPageNum(pageNumber)
                            .setKey(
                                    FeatureValue.newBuilder()
                                            .setId(keyBlock.id())
                                            .setText(fieldName)
                                            .setGeometry(
                                                    toGeometry(keyBlock, imagePreprocessingResult))
                                            .build())
                            .setValue(
                                    FeatureValue.newBuilder()
                                            .setId(valueBlock.id())
                                            .setText(valueText)
                                            .setGeometry(
                                                    toGeometry(
                                                            valueBlock, imagePreprocessingResult))
                                            .build())
                            .setExtractedDataType(DataType.TEXT)
                            .build());
        }
    }

    private void toGraphicFeatures(
            Map<BlocksByType, BlocksByType> blocksByTypeMap,
            ExtractionResult.Builder extractionResultBuilder,
            ImagePreprocessingResult imagePreprocessingResult) {

        /* GraphicFeature.Builder graphicBuilder = GraphicFeature.newBuilder();
        graphicBuilder.setId();
        graphicBuilder.setDomainFieldKey();
        graphicBuilder.setDomainFieldKey();
        graphicBuilder.setOcrConfidence();*/

        // extractionResultBuilder.addGraphicFeatures()
    }

    private void toTables(
            Map<BlocksByType, BlocksByType> blocksByTypeMap,
            ExtractionResult.Builder extractionResultBuilder,
            ImagePreprocessingResult imagePreprocessingResult,
            Domain domain) {

        BlocksByType tableBlocks = blocksByTypeMap.get(new BlocksByType(BlockType.TABLE));
        Map<BlocksById, Block> tableBlockMap = tableBlocks.getBlockIdMap();

        BlocksByType cellBlocks = blocksByTypeMap.get(new BlocksByType(BlockType.CELL));
        Map<BlocksById, Block> cellBlockIdMap = cellBlocks.getBlockIdMap();

        BlocksByType wordBlocks = blocksByTypeMap.get(new BlocksByType(BlockType.WORD));
        Map<BlocksById, Block> wordBlockIdMap = wordBlocks.getBlockIdMap();

        for (Map.Entry<BlocksById, Block> tableBlockEntry : tableBlockMap.entrySet()) {
            Block tableBlock = tableBlockEntry.getValue();
            Relationship relationship =
                    tableBlock.relationships().stream()
                            .filter(r -> r.type() == RelationshipType.CHILD)
                            .findFirst()
                            .get();
            List<String> tableChildBlockIds = relationship.ids();

            Map<Integer, String> headerMap = new HashMap<>();
            Map<Integer, Map<Integer, Block>> rowBlockMap = new TreeMap<>();
            Map<Integer, Block> headerBlockMap = new TreeMap<>();
            for (String blockId : tableChildBlockIds) {
                Block cellBlock = cellBlockIdMap.get(new BlocksById(blockId));

                if (cellBlock.entityTypes().contains(EntityType.COLUMN_HEADER)) {
                    List<Block> childBlocks =
                            getBlocks(wordBlockIdMap, cellBlock, RelationshipType.CHILD);
                    List<String> childValues =
                            childBlocks.stream().map(Block::text).collect(Collectors.toList());
                    String text = String.join(" ", childValues);

                    String fieldName =
                            findKeyByAlias(text, domain.getTables().get(0).getTableHeaders());
                    headerMap.put(cellBlock.columnIndex(), fieldName);
                    if (fieldName == null) { // skip if the domain doesn't contain the field
                        continue;
                    }
                    headerBlockMap.put(cellBlock.columnIndex(), cellBlock);
                } else {

                    Map<Integer, Block> cellMap = rowBlockMap.get(cellBlock.rowIndex());
                    if (cellMap == null) {
                        cellMap = new TreeMap<>();
                        rowBlockMap.put(cellBlock.rowIndex(), cellMap);
                    }
                    cellMap.put(cellBlock.columnIndex(), cellBlock);
                }
            }

            // headers
            TableFeature.Builder tableFeatureBuilder = TableFeature.newBuilder();
            tableFeatureBuilder.setId(tableBlock.id());
            for (Map.Entry<Integer, Block> headerBlockEntry : headerBlockMap.entrySet()) {
                Block headerBlock = headerBlockEntry.getValue();
                String fieldName =
                        headerMap.get(
                                headerBlock.columnIndex()); // cell index mapped with header column
                int pageNumber = getPageNumber(headerBlock);

                TableHeaderFeature tableHeaderFeature =
                        TableHeaderFeature.newBuilder()
                                .setId(headerBlock.id())
                                .setDomainFieldKey(fieldName)
                                .setText(fieldName)
                                .setGeometry(toGeometry(headerBlock, imagePreprocessingResult))
                                .setPageNum(pageNumber)
                                .build();
                tableFeatureBuilder.addHeaders(tableHeaderFeature);
            }

            // rows
            if (headerMap.size() > 0) {
                for (Map.Entry<Integer, Map<Integer, Block>> rowEntry : rowBlockMap.entrySet()) {

                    Integer rowNo = rowEntry.getKey();
                    Map<Integer, Block> cellMap = rowEntry.getValue();
                    TableRowFeature.Builder tableRowFeatureBuilder = TableRowFeature.newBuilder();

                    for (Map.Entry<Integer, Block> cellEntry : cellMap.entrySet()) {
                        Block block = cellEntry.getValue();
                        List<Block> valueBlocks =
                                getBlocks(wordBlockIdMap, block, RelationshipType.CHILD);
                        String valueText = " ";
                        if (valueBlocks != null) {
                            List<String> valueList =
                                    valueBlocks.stream()
                                            .map(Block::text)
                                            .collect(Collectors.toList());
                            valueText = String.join(" ", valueList);
                        }
                        String fieldName = headerMap.get(block.columnIndex());
                        if (fieldName == null) {
                            continue;
                        }
                        int pageNumber = getPageNumber(block);
                        TableCellFeature.Builder tableCellFeatureBuilder =
                                TableCellFeature.newBuilder()
                                        .setId(block.id())
                                        .setDomainFieldKey(fieldName)
                                        .setText(valueText)
                                        .setExtractedDataType(DataType.TEXT)
                                        .setGeometry(toGeometry(block, imagePreprocessingResult))
                                        .setPageNum(pageNumber);

                        TableCellFeature tableCellFeature = tableCellFeatureBuilder.build();

                        tableRowFeatureBuilder.setPageNum(pageNumber).addCells(tableCellFeature);
                    }
                    tableFeatureBuilder.addRows(tableRowFeatureBuilder.build());
                }
                extractionResultBuilder.addTableFeatures(tableFeatureBuilder.build());
            }
        }
    }

    private void toTablesOld(
            Map<BlocksByType, BlocksByType> blocksByTypeMap,
            ExtractionResult.Builder extractionResultBuilder,
            ImagePreprocessingResult imagePreprocessingResult,
            Domain domain) {

        TableFeature.Builder tableFeatureBuilder = TableFeature.newBuilder();

        BlocksByType wordBlocks = blocksByTypeMap.get(new BlocksByType(BlockType.WORD));
        Map<BlocksById, Block> wordBlockMap = wordBlocks.getBlockIdMap();

        BlocksByType cellBlocks = blocksByTypeMap.get(new BlocksByType(BlockType.CELL));
        Map<BlocksById, Block> cellBlocksMap = cellBlocks.getBlockIdMap();

        // GET rows and columns in a Map
        Map<Integer, Map<Integer, Block>> rowMap = new TreeMap<>();
        Map<Integer, Block> headerRowMap = new TreeMap<>();
        for (Map.Entry<BlocksById, Block> cellBlockEntry : cellBlocksMap.entrySet()) {
            Block block = cellBlockEntry.getValue();

            if (block.entityTypes().contains(EntityType.COLUMN_HEADER)) {
                headerRowMap.put(block.columnIndex(), block);
            } else {
                Map<Integer, Block> cellMap = rowMap.get(block.rowIndex());
                if (cellMap == null) {
                    cellMap = new TreeMap<>();
                    rowMap.put(block.rowIndex(), cellMap);
                }
                cellMap.put(block.columnIndex(), block);
            }
        }

        Map<Integer, String> headerMap = new HashMap<>();

        for (Map.Entry<Integer, Block> headerEntry : headerRowMap.entrySet()) {
            Block block = headerEntry.getValue();

            List<Block> keyBlocks = getBlocks(wordBlockMap, block, RelationshipType.CHILD);
            List<String> keyList = keyBlocks.stream().map(Block::text).collect(Collectors.toList());
            String keyText = String.join(" ", keyList);

            String fieldName = findKeyByAlias(keyText, domain.getTables().get(0).getTableHeaders());
            if (fieldName == null
                    || fieldName.isEmpty()) { // if the alias in not found in domain  skip the field
                continue;
            }

            headerMap.put(block.columnIndex(), fieldName); // cell index mapped with header column

            int pageNumber = getPageNumber(block);

            TableHeaderFeature tableHeaderFeature =
                    TableHeaderFeature.newBuilder()
                            .setId(block.id())
                            .setDomainFieldKey(fieldName)
                            .setText(fieldName)
                            .setGeometry(toGeometry(block, imagePreprocessingResult))
                            .setPageNum(pageNumber)
                            .build();
            tableFeatureBuilder.addHeaders(tableHeaderFeature);
        }

        for (Map.Entry<Integer, Map<Integer, Block>> rowEntry : rowMap.entrySet()) {

            Integer rowNo = rowEntry.getKey();
            Map<Integer, Block> cellMap = rowEntry.getValue();

            TableRowFeature.Builder tableRowFeatureBuilder = TableRowFeature.newBuilder();

            for (Map.Entry<Integer, Block> cellEntry : cellMap.entrySet()) {
                Block block = cellEntry.getValue();
                List<Block> valueBlocks = getBlocks(wordBlockMap, block, RelationshipType.CHILD);
                String valueText = " ";
                if (valueBlocks != null) {
                    List<String> valueList =
                            valueBlocks.stream().map(Block::text).collect(Collectors.toList());
                    valueText = String.join(" ", valueList);
                }
                String fieldName = headerMap.get(block.columnIndex());
                if (fieldName == null) {
                    continue;
                }
                int pageNumber = getPageNumber(block);
                TableCellFeature.Builder tableCellFeatureBuilder =
                        TableCellFeature.newBuilder()
                                .setId(block.id())
                                .setDomainFieldKey(fieldName)
                                .setText(valueText)
                                .setExtractedDataType(DataType.TEXT)
                                .setGeometry(toGeometry(block, imagePreprocessingResult))
                                .setPageNum(pageNumber);

                TableCellFeature tableCellFeature = tableCellFeatureBuilder.build();

                tableRowFeatureBuilder.setPageNum(pageNumber).addCells(tableCellFeature);
            }
            tableFeatureBuilder.addRows(tableRowFeatureBuilder.build());
        }
        extractionResultBuilder.addTableFeatures(tableFeatureBuilder.build());
    }

    private EngineMetadata toEngineMetadata(
            GetDocumentAnalysisResponse analyzeDocument, String inputImageFilePath) {

        return EngineMetadata.newBuilder()
                .setFilepath(inputImageFilePath)
                .setNumberOfPages(analyzeDocument.documentMetadata().pages())
                .setExecutionStatus(
                        ExecutionStatus.newBuilder()
                                .setStatusCode(HttpStatus.SC_OK)
                                .setStatusMessage("SUCCESS")
                                .setMessage("Document processed successfully")
                                .build())
                .build();
    }

    private OcrBlock toOCRBlock(Block block, ImagePreprocessingResult imagePreprocessingResult) {

        OcrBlock.Builder ocrBlockBuilder =
                OcrBlock.newBuilder()
                        .setId(block.id())
                        .setGeometry(toGeometry(block, imagePreprocessingResult))
                        .setBlockType(toBlockType(block.blockType()))
                        .setPageNum(block.page() == null ? 1 : block.page())
                        .setConfidence(block.confidence());

        if (block.text() != null && !block.text().isEmpty()) {
            ocrBlockBuilder.setText(block.text());
        }

        return ocrBlockBuilder.build();
    }

    private OcrBlockType toBlockType(BlockType blockType) {

        OcrBlockType ocrBlockType;
        switch (blockType) {
            case LINE:
                ocrBlockType = OcrBlockType.LINE;
                break;
            case SIGNATURE:
                ocrBlockType = OcrBlockType.UNKNOWN_BLOCK;
                break;
            default:
                ocrBlockType = OcrBlockType.WORD;
                break;
        }
        return ocrBlockType;
    }

    private int getPageNumber(Block block) {
        int pageNumber = block.page() == 0 || block.page() == null ? 1 : block.page();
        return pageNumber;
    }

    private Geometry toGeometry(Block block, ImagePreprocessingResult imagePreprocessingResult) {

        software.amazon.awssdk.services.textract.model.Geometry geometry = block.geometry();
        List<Point> points = geometry.polygon();
        Point point1 = points.get(0);
        Point point2 = points.get(2);

        int pageNumber = getPageNumber(block);
        ImagePreprocessingPage imagePreprocessingPage =
                imagePreprocessingResult.getPages(pageNumber - 1);
        int height = imagePreprocessingPage.getHeight();
        int width = imagePreprocessingPage.getWidth();

        // TODO : use page width and height to get OCR Block co-ordinates

        return Geometry.newBuilder()
                .setX1(Double.valueOf(point1.x() * width).intValue())
                .setY1(Double.valueOf(point1.y() * height).intValue())
                .setX2(Double.valueOf(point2.x() * width).intValue())
                .setY2(Double.valueOf(point2.y() * height).intValue())
                .build();
    }

    private DocDetectResult toDocDetectResult(GetDocumentAnalysisResponse analyzeDocument) {
        return DocDetectResult.newBuilder()
                .setMetadata(
                        DocDetectMetadata.newBuilder()
                                .setExecutionStatus(
                                        ExecutionStatus.newBuilder()
                                                .setStatusCode(HttpStatus.SC_OK)
                                                .setStatusMessage("SUCCESS")
                                                .setMessage("Document processed successfully")
                                                .build())
                                .build())
                .build();
        // TODO : feature objects
    }

    private String findKeyByAlias(String alias, List<Field> fields) {

        for (Field field : fields) {
            Optional<String> optionalAlias =
                    field.getDefaultAliases().stream()
                            .filter(a -> a.equalsIgnoreCase(alias))
                            .findFirst();
            if (optionalAlias.isPresent()) {
                return field.getName();
            }
        }
        return null;
    }

    private List<String> getImagePaths(String inputFilePath) {

        String fileName =
                inputFilePath.substring(inputFilePath.lastIndexOf(File.separatorChar) + 1);
        String folderPath =
                inputFilePath.substring(0, (inputFilePath.length() - fileName.length() - 1));
        DocumentSplitter splitter = DocumentSplitterFactory.create(inputFilePath);

        try {
            List<String> imagePaths =
                    splitter.split(inputFilePath, folderPath, Collections.emptyMap());
            return imagePaths;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    class DocumentPageMetaData {

        private int height;
        private int width;

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }
    }

    class BlocksByType {
        private BlockType blockType;
        private Map<BlocksById, Block> blockIdMap = new HashMap<>();

        public BlockType getBlockType() {
            return blockType;
        }

        public Map<BlocksById, Block> getBlockIdMap() {
            return blockIdMap;
        }

        public BlocksByType(BlockType blockType) {
            this.blockType = blockType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BlocksByType that = (BlocksByType) o;
            return blockType == that.blockType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(blockType);
        }
    }

    class BlocksById {
        private String blockId;
        private Block block;

        public BlocksById(String blockId) {
            this.blockId = blockId;
        }

        public BlocksById(String blockId, Block block) {
            this.blockId = blockId;
            this.block = block;
        }

        public String getBlockId() {
            return blockId;
        }

        public Block getBlock() {
            return block;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BlocksById that = (BlocksById) o;
            return blockId.equals(that.blockId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(blockId);
        }
    }
}
