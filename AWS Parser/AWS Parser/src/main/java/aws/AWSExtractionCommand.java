/* Copyright (c) 2023 Automation Anywhere. All rights reserved.
 *
 * This software is the proprietary information of Automation Anywhere. You shall use it only in
 * accordance with the terms of the license agreement you entered into with Automation Anywhere.
 */
package aws;

/*import static com.automationanywhere.commandsdk.model.DataType.STRING;*/

import static com.automationanywhere.commandsdk.model.DataType.STRING;

import aws.application.service.AWSExtractionService;
import aws.application.transformer.DocumentAnalysisResponseDataTransformer;
import com.automationanywhere.bot.service.GlobalSessionContext;
import com.automationanywhere.botcommand.data.impl.StringValue;
import com.automationanywhere.commandsdk.annotations.*;
import com.automationanywhere.commandsdk.annotations.rules.LocalFile;
import com.automationanywhere.commandsdk.annotations.rules.NotEmpty;
import com.automationanywhere.commandsdk.model.AttributeType;
import com.automationanywhere.core.security.SecureString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@BotCommand
@CommandPkg(
        name = "AWSExtractionCommand",
        label = "AWS Extraction Command",
        description = "AWS Extraction Command",
        node_label = "AWS Extraction Command",
        return_type = STRING,
        return_label = "AWS Extraction Command Response",
        minimum_botagent_version = "21.98",
        minimum_controlroom_version = "10520")
public class AWSExtractionCommand {

    private static final Logger LOGGER = LogManager.getLogger();

    @com.automationanywhere.commandsdk.annotations.GlobalSessionContext
    private GlobalSessionContext globalSessionContext;

    private AWSExtractionService awsExtractionService;

    public AWSExtractionCommand() {
        this(new AWSExtractionService(new DocumentAnalysisResponseDataTransformer()));
    }

    public AWSExtractionCommand(AWSExtractionService awsExtractionService) {
        this.awsExtractionService = awsExtractionService;
    }

    public void setGlobalSessionContext(final GlobalSessionContext globalSessionContext) {
        this.globalSessionContext = globalSessionContext;
    }

    @Execute
    public StringValue compute(
            @Idx(index = "1", type = AttributeType.FILE)
                    @LocalFile
                    @Pkg(label = "Image File Path")
                    @NotEmpty
                    final String inputFilePath,
            @Idx(index = "2", type = AttributeType.CREDENTIAL) @Pkg(label = "Service Account")
                    final SecureString serviceAccount) {
        LOGGER.info("DA CMD: Bot command input: inputImageFilePath: {}", inputFilePath);

        return new StringValue(extract(inputFilePath, serviceAccount));
    }

    public String extract(String inputImageFilePath, SecureString serviceAccount) {
        try {
            return awsExtractionService.extract(
                    inputImageFilePath, serviceAccount.getInsecureString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
