package io.corexchain.verifyservice.issuer.service;

import io.corexchain.verify4j.PdfIssuer;
import io.corexchain.verify4j.exceptions.AlreadyExistsException;
import io.corexchain.verify4j.exceptions.InvalidCreditAmountException;
import io.corexchain.verifyservice.issuer.controller.IssuerController;
import io.corexchain.verifyservice.issuer.exceptions.BadRequestException;
import io.corexchain.verifyservice.issuer.model.IssueResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.Date;

@Service
public class IssuerService {
    private static final Logger logger = LoggerFactory.getLogger(IssuerController.class);


    @Value("${verify.service.blockchain.node.url}")
    private String nodeUrl;
    @Value("${verify.service.blockchain.contract.address}")
    private String contractAddress;
    @Value("${verify.service.blockchain.issuer.address}")
    private String issuerAddress;
    @Value("${verify.service.blockchain.issuer.pk}")
    private String issuerPk;


    @Value("${verify.service.blockchain.issuer.name}")
    private String issuerName;

    @Value("${verify.service.blockchain.testnet.node.url}")
    private String nodeUrlTest;
    @Value("${verify.service.blockchain.testnet.contract.address}")
    private String contractAddressTest;
    @Value("${verify.service.blockchain.testnet.issuer.address}")
    private String issuerAddressTest;
    @Value("${verify.service.blockchain.testnet.issuer.pk}")
    private String issuerPkTest;

    public IssueResponseDTO issue(String sourcePath, String destPath, Date expireDate, String desc) throws Exception {
        String expireDateStr = expireDate != null ? expireDate.toString() : "-";
        logger.info("Issuing file src: " + sourcePath + ", dest: " + destPath + ", expireDate: " + expireDateStr + ", desc: " + desc);
        try {
            PdfIssuer issuer = new PdfIssuer(contractAddress, issuerAddress, issuerName, nodeUrl, 1104);
            String txHash = issuer.issue("", sourcePath, destPath, expireDate, desc, "", issuerPk);
            logger.info("Success with hash: " + txHash);
            return new IssueResponseDTO(txHash);
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
            throw new BadRequestException("Source file or destination file path is wrong");
        } catch (AlreadyExistsException e) {
            logger.error(e.getMessage());
            throw new BadRequestException("File hash is already written on blockchain");
        } catch (InvalidCreditAmountException e) {
            logger.error(e.getMessage());
            throw new BadRequestException("Not enough credit");
        }
        catch (BadRequestException e) {
            logger.error(e.getMessage());
            throw new BadRequestException(e.getMessage());
        }
    }

    public IssueResponseDTO issueTest(String sourcePath, String destPath, Date expireDate, String desc) throws Exception {
        String expireDateStr = expireDate != null ? expireDate.toString() : "-";
        logger.info("Issuing test file src: " + sourcePath + ", dest: " + destPath + ", expireDate: " + expireDateStr + ", desc: " + desc);
        try {
            PdfIssuer issuer = new PdfIssuer(contractAddressTest, issuerAddressTest, issuerName, nodeUrlTest, 3305);
            String txHash = issuer.issue("", sourcePath, destPath, expireDate, desc, "", issuerPkTest);
            logger.info("Success with hash: " + txHash);
            return new IssueResponseDTO(txHash);
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
            throw new BadRequestException("Source file or destination file path is wrong");
        } catch (AlreadyExistsException e) {
            logger.error(e.getMessage());
            throw new BadRequestException("File hash is already written on blockchain");
        } catch (InvalidCreditAmountException e) {
            logger.error(e.getMessage());
            throw new BadRequestException("Not enough credit");
        }
        catch (BadRequestException e) {
            logger.error(e.getMessage());
            throw new BadRequestException(e.getMessage());
        }
    }

}
