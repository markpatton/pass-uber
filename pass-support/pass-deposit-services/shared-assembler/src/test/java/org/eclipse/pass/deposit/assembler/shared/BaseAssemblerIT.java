/*
 * Copyright 2018 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eclipse.pass.deposit.assembler.shared;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.pass.deposit.DepositTestUtil.openArchive;
import static org.eclipse.pass.deposit.DepositTestUtil.packageFile;
import static org.eclipse.pass.deposit.DepositTestUtil.savePackage;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.eclipse.pass.deposit.assembler.PackageStream;
import org.eclipse.pass.deposit.builder.InvalidModel;
import org.eclipse.pass.deposit.builder.SubmissionBuilder;
import org.eclipse.pass.deposit.builder.fs.FilesystemModelBuilder;
import org.eclipse.pass.deposit.model.DepositFile;
import org.eclipse.pass.deposit.model.DepositSubmission;
import org.eclipse.pass.deposit.assembler.PackageOptions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import resources.SharedSubmissionUtil;

/**
 * Abstract integration test for {@link AbstractAssembler} implementations.
 * <p>
 * Creates and extracts a package using the {@link #assemblerUnderTest() assembler under test}.  Subclasses have access
 * to the extracted package directory as {@link #extractedPackageDir}, and to the contents being packaged (in various
 * forms):
 * <ul>
 *     <li>{@link #custodialResources}: a simple {@code List} of Spring {@link Resource}s</li>
 *     <li>{@link #custodialResourcesMap}: a {@code Map} of Spring {@link Resource}s, keyed by resource name</li>
 * </ul>
 * </p>
 */
public abstract class BaseAssemblerIT {

    @Rule
    public TestName testName = new TestName();

    protected static final Logger LOG = LoggerFactory.getLogger(BaseAssemblerIT.class);

    protected SharedSubmissionUtil submissionUtil;

    protected SubmissionBuilder builder;

    /**
     * The custodial resources that are to be packaged up by {@link #setUp()}.  They should be present in the extracted
     * package.
     * <p>
     * It should be noted that some implementations (notably the {@code NihmsZippedPackageStream}) will remediate the
     * filenames of the custodial resources.  For example, if the name of a custodial resource conflicts with the name
     * of a file that is required by a packaging specification, then the assembler implementation may remediate the
     * conflict by re-naming or moving the custodial resource to a new location within the package.  <strong>The
     * custodial resource will <em>not</em> be updated with the remediated file location</strong>.  Subclasses of this
     * integration test will need to be aware that the resources in this list, and any other data structures that
     * contain custodial resource (e.g. {@link #custodialResourcesMap}) will not
     * contain remediated resource names; the resources will be known by their original names..
     * </p>
     */
    protected List<DepositFile> custodialResources;

    /**
     * The custodial resources that are to be packaged up by {@link #setUp()}, keyed by file name.  They should be
     * present in the extracted package
     */
    protected Map<String, DepositFile> custodialResourcesMap;

    /**
     * The package generated by {@link #setUp()} is extracted to this directory
     */
    protected File extractedPackageDir;

    /**
     * The {@link ResourceBuilderFactory} used by the {@link AbstractAssembler} to create {@link
     * PackageStream.Resource}s from the {@link #custodialResources custodial resources}
     */
    protected ResourceBuilderFactory rbf;

    /**
     * The {@link MetadataBuilderFactory} used by the {@link AbstractAssembler} to create {@link
     * PackageStream.Metadata}
     */
    protected MetadataBuilderFactory mbf;

    /**
     * The submission that the {@link #extractedPackageDir extracted package} is composed from
     */
    protected DepositSubmission submission;

    /**
     * Mocks a submission, and invokes the assembler to create a package based on the resources under the
     * {@code sample1/} resource path.  Sets the {@link #extractedPackageDir} to the base directory of the newly created
     * and extracted package.
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        submissionUtil = new SharedSubmissionUtil();
        builder = new FilesystemModelBuilder(null);
        mbf = metadataBuilderFactory();
        rbf = resourceBuilderFactory();
        AbstractAssembler underTest = assemblerUnderTest();

        prepareSubmission();

        prepareCustodialResources();

        PackageStream stream = underTest.assemble(submission, getOptions());

        File packageArchive = savePackage(packageFile(this.getClass(), testName, stream.metadata()), stream);

        verifyStreamMetadata(stream.metadata());

        extractPackage(packageArchive, stream.metadata().archive(), stream.metadata().compression());
    }

    protected abstract Map<String, Object> getOptions();

    protected void prepareSubmission() throws InvalidModel, IOException {
        prepareSubmission(URI.create("fake:submission1"));
    }

    protected void prepareSubmission(URI submissionUri) throws InvalidModel, IOException {
        submission = submissionUtil.asDepositSubmission(submissionUri, builder);
    }

    /**
     * Obtains a List of Resources from the classpath, stores them in {@link #custodialResources}.
     *
     * Creates a convenience {@code Map}, mapping file names to their corresponding Resources in {@link
     * #custodialResourcesMap}.  Every Resource in {@code custodialResources} should be represented in {@code
     * custodialResourcesMap}, and vice-versa.
     *
     * @return a {@code Map} of custodial resources to be packaged, and their corresponding {@code DepositFileType}
     */
    protected List<DepositFile> prepareCustodialResources() {
        // Insure we're packaging something
        assertTrue("Refusing to create an empty package!", submission.getFiles().size() > 0);
        custodialResources = submission.getFiles();
        custodialResourcesMap = submission.getFiles().stream().collect(toMap(DepositFile::getName, identity()));
        return submission.getFiles();
    }

    /**
     * Extracts the supplied package archive file (.zip, .gzip, etc) to the {@link #extractedPackageDir}.
     *
     * @param packageArchive the package archive file to open
     * @throws IOException if there is an error opening the package
     */
    protected void extractPackage(File packageArchive, PackageOptions.Archive.OPTS archive, PackageOptions.Compression.OPTS compression)
        throws IOException {
        extractedPackageDir = openArchive(packageArchive, archive, compression);

        LOG.debug(">>>> Extracted package to '{}'", extractedPackageDir);
    }

    /**
     * Returns a new instance of the {@link DefaultMetadataBuilderFactory}
     *
     * @return
     */
    protected static MetadataBuilderFactory metadataBuilderFactory() {
        return new DefaultMetadataBuilderFactory();
    }

    /**
     * Returns a new instance of the {@link DefaultResourceBuilderFactory}
     *
     * @return
     */
    protected static ResourceBuilderFactory resourceBuilderFactory() {
        return new DefaultResourceBuilderFactory();
    }

    /**
     * To be implemented by sub-classes: must return a fully functional instance of the {@link AbstractAssembler} to be
     * tested.
     *
     * @return the {@code AbstractAssembler} under test
     */
    protected abstract AbstractAssembler assemblerUnderTest();

    /**
     * To be implemented by sub-classes: must verify expected values found in the {@link PackageStream.Metadata}.
     *
     * @param metadata the package stream metadata
     */
    protected abstract void verifyStreamMetadata(PackageStream.Metadata metadata);
}
