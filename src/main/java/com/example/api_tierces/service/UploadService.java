/*package com.example.api_tierces.service;

public interface UploadService {
    String parseSwaggerFile(String fileContent);
}
*/
package com.example.api_tierces.service;

public interface UploadService {

    String parseSwaggerFileFromUrl(String swaggerUrl);
    String parseSwaggerFile(String fileContent);

}