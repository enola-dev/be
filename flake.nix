{
  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";

  outputs =
    { self, nixpkgs }:
    let
      system = "x86_64-linux";
      pkgs = import nixpkgs { inherit system; };
      filteredSrc = pkgs.lib.cleanSourceWith {
        src = self;
      };
    in
    {
      devShells.${system}.default = pkgs.mkShell {
        packages = with pkgs; [
          pre-commit
          nixfmt
          temurin-bin-25
        ];
      };

      checks.${system}.test-script =
        pkgs.runCommand "test-script-check"
          {
            buildInputs = with pkgs; [
              temurin-bin-25
            ];
          }
          ''
            cp -r ${filteredSrc}/* .
            chmod +x test.sh
            ${pkgs.bash}/bin/bash ./test.sh > $out
          '';

      formatter.x86_64-linux = pkgs.nixfmt-tree;
    };
}
