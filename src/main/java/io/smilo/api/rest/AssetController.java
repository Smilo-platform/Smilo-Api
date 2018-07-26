package io.smilo.api.rest;

import io.smilo.api.block.data.assets.AssetDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AssetController {
    @GetMapping("/asset")
    public String[] listAssets() {
        return new String[]{"XSM", "XSP"};
    }

    @GetMapping("/asset/{assetSymbol}")
    public AssetDTO getAsset(@PathVariable("assetSymbol") String assetSymbol) {
        if(assetSymbol.equals("XSM")) {
            return new AssetDTO(200000000L, "Smilo", 0, "XSM");
        }
        else if(assetSymbol.equals("XSP")) {
            // Not yet correct, total supply should be BigInt!
            // Must be updated once core uses BigInts for balance.
            return new AssetDTO(200000000L, "SmiloPay", 18, "XSP");
        }
        else {
            // This is an unknown asset
            throw new AssetNotFoundException();
        }
    }

    @ResponseStatus(value=HttpStatus.NOT_FOUND, reason="Asset not found")  // 404
    public class AssetNotFoundException extends RuntimeException {
        // Empty on purpose
    }
}
