<#
.SYNOPSIS
    Pose la section d'une feature dans CHANGELOGS.md : frontmatter, squelette
    du détail et table des commits.

.DESCRIPTION
    Une branche `feat/...` (ou `feature/...`) est une feature : tous les commits
    propres à cette branche par rapport à la branche de base forment la feature.
    Le script en tire le nom, la date, les auteurs et la liste des commits, puis
    insère la section en tête du journal :

        ---
        feature: <nom de la feature>
        branche: feat/<nom-court>
        base: main
        date: 2026-10-15
        auteurs:
          - Nom Prénom
        commits: 5
        ---

        ## <nom de la feature>

        ### Détail

        _À compléter._

        ### Commits

        | Commit | Sujet |
        |---|---|
        | `abc1234` | feat(x): ... |

    Le contenu de la section « Détail » est ensuite rédigé par la commande
    opencode /nouvelle-changelog, à partir du diff et des corps de commit. Ce
    script ne fait que poser le squelette : il ne réécrit jamais une section
    déjà rédigée.

.PARAMETER Base
    Branche de référence servant au calcul du point de comparaison. Par défaut
    `main`.

.PARAMETER Branche
    Branche dont les commits sont listés. Par défaut la branche courante.

.PARAMETER Titre
    Nom de la feature. Par défaut, déduit du nom de la branche (préfixe
    `feat/`, `feature/`, `fix/`, `bugfix/`, `hotfix/`, `chore/`, `docs/` retiré,
    tirets et underscores remplacés par des espaces).

.PARAMETER Date
    Date de la section au format `yyyy-MM-dd`. Par défaut, la date du jour.

.PARAMETER Chemin
    Fichier journal à mettre à jour. Par défaut `CHANGELOGS.md` à la racine.

.PARAMETER Apercu
    Affiche la section sans écrire dans le fichier.

.EXAMPLE
    ./scripts/nouvelle-changelog.ps1 -Apercu

.EXAMPLE
    ./scripts/nouvelle-changelog.ps1 -Branche feat/evaluation -Titre "Évaluation des soutenances"

.EXAMPLE
    ./scripts/nouvelle-changelog.ps1 -Branche feat/jury -Base origin/main -Chemin CHANGELOGS.md
#>

[CmdletBinding()]
param(
    [string] $Base = 'main',
    [string] $Branche,
    [string] $Titre,
    [string] $Date,
    [string] $Chemin,
    [switch] $Apercu
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$PrefixesFeature = @('feat', 'feature')
$PrefixesConnus = @('feat', 'feature', 'fix', 'bugfix', 'hotfix', 'chore', 'docs', 'refactor', 'test', 'perf')

function Invoke-Git {
    param([Parameter(Mandatory)][string[]] $Arguments)

    $sortie = & git @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "git $($Arguments -join ' ') a échoué : $sortie"
    }
    return @($sortie | ForEach-Object { [string] $_ })
}

function Get-PrefixeBranche {
    param([Parameter(Mandatory)][string] $NomBranche)

    if ($NomBranche -match '^([^/]+)/') { return $Matches[1].ToLowerInvariant() }
    return ''
}

function Get-TitreParDefaut {
    param([Parameter(Mandatory)][string] $NomBranche)

    $nom = $NomBranche -replace '^[^/]+/', ''
    $nom = $nom -replace '[-_]+', ' '
    $nom = ($nom -replace '\s+', ' ').Trim()
    if ([string]::IsNullOrWhiteSpace($nom)) { return $NomBranche }
    return $nom.Substring(0, 1).ToUpperInvariant() + $nom.Substring(1)
}

$racine = @(Invoke-Git @('rev-parse', '--show-toplevel'))[0].Trim()
Push-Location $racine
trap {
    Write-Host $_.Exception.Message -ForegroundColor Red
    Pop-Location
    exit 1
}
try {
    if (-not $Branche) {
        $Branche = @(Invoke-Git @('rev-parse', '--abbrev-ref', 'HEAD'))[0].Trim()
    }
    if ($Branche -eq 'HEAD') {
        throw "HEAD est détaché : indique -Branche pour lister les commits d'une branche précise."
    }

    foreach ($ref in @($Branche, $Base)) {
        & git rev-parse --verify --quiet $ref *> $null
        if ($LASTEXITCODE -ne 0) {
            throw "La référence '$ref' n'existe pas localement. Fetch ou corrige le paramètre -Base."
        }
    }

    $prefixe = Get-PrefixeBranche -NomBranche $Branche
    if ($prefixe -and $PrefixesConnus -notcontains $prefixe) {
        Write-Host "Préfixe de branche '$prefixe' non reconnu (attendu : $($PrefixesConnus -join ', '))." -ForegroundColor Yellow
    }
    $estFeature = $PrefixesFeature -contains $prefixe

    $fusion = @(Invoke-Git @('merge-base', $Base, $Branche))[0].Trim()
    $plage = "$fusion..$Branche"
    $lignes = @(Invoke-Git @('log', '--reverse', '--no-merges', '--format=%h%x09%s', $plage))
    if ($lignes.Count -eq 0) {
        throw "Aucun commit propre à '$Branche' par rapport à '$Base' : rien à journaliser."
    }

    $auteurs = @(Invoke-Git @('log', '--reverse', '--no-merges', '--format=%an', $plage) |
        ForEach-Object { $_.Trim() } | Where-Object { $_ } | Select-Object -Unique)

    if (-not $Date) { $Date = (Get-Date).ToString('yyyy-MM-dd') }
    if ($Date -notmatch '^\d{4}-\d{2}-\d{2}$') {
        throw "Date invalide '$Date' : utilise le format yyyy-MM-dd."
    }
    if (-not $Titre) {
        $Titre = if ($estFeature) { Get-TitreParDefaut -NomBranche $Branche } else { "$prefixe — $(Get-TitreParDefaut -NomBranche $Branche)" }
    }
    if (-not $Chemin) { $Chemin = Join-Path $racine 'CHANGELOGS.md' }

    $table = [System.Text.StringBuilder]::new()
    [void] $table.AppendLine('| Commit | Sujet |')
    [void] $table.AppendLine('|---|---|')
    foreach ($ligne in $lignes) {
        $parties = $ligne -split "`t", 2
        $sujet = if ($parties.Count -gt 1) { $parties[1] } else { '' }
        [void] $table.AppendLine("| ``$($parties[0].Trim())`` | $sujet |")
    }

    $frontmatter = @(
        '---'
        "feature: $Titre"
        "branche: $Branche"
        "base: $Base"
        "date: $Date"
        'auteurs:'
        ($auteurs | ForEach-Object { "  - $_" })
        "commits: $($lignes.Count)"
        '---'
    ) -join "`n"

    $section = @(
        $frontmatter
        ''
        "## $Titre"
        ''
        '### Détail'
        ''
        '_À compléter : intention de la feature, périmètre livré, choix techniques, écarts au plan._'
        ''
        '### Commits'
        ''
        $table.ToString().TrimEnd()
        ''
    ) -join "`n"

    $type = if ($estFeature) { 'feature' } else { "précfixe '$prefixe'" }
    Write-Host "$type — branche '$Branche', $($lignes.Count) commits, auteur(s) : $($auteurs -join ', ')" -ForegroundColor Cyan
    Write-Host ''
    Write-Host $section

    if ($Apercu) {
        Write-Host '[-Apercu] Aucune écriture.' -ForegroundColor DarkGray
        return
    }

    if (-not (Test-Path -LiteralPath $Chemin)) {
        throw "Journal introuvable : $Chemin"
    }

    $contenu = [System.IO.File]::ReadAllText($Chemin)
    $motif = '(?m)^##\s+' + [regex]::Escape($Titre) + '\s*$'
    if ($contenu -match $motif) {
        throw "Une section '$Titre' existe déjà dans $Chemin. Utilise -Apercu ou change -Titre."
    }

    $lignesFichier = $contenu -split "`r?`n"
    $index = $lignesFichier.Count

    # Un frontmatter de tête de fichier n'est pas une section : on le saute.
    $i = 0
    if ($lignesFichier.Count -gt 0 -and $lignesFichier[0].TrimEnd() -eq '---') {
        $i = 1
        while ($i -lt $lignesFichier.Count -and $lignesFichier[$i].TrimEnd() -ne '---') { $i++ }
        $i++
    }
    for (; $i -lt $lignesFichier.Count; $i++) {
        if ($lignesFichier[$i] -match '^##\s+\S' -or $lignesFichier[$i].TrimEnd() -eq '---') {
            $index = $i
            break
        }
    }

    $avant = if ($index -eq 0) { @() } else { @($lignesFichier[0..($index - 1)]) }
    $apres = @($lignesFichier[$index..($lignesFichier.Count - 1)])

    while ($avant.Count -gt 0 -and [string]::IsNullOrWhiteSpace($avant[-1])) {
        if ($avant.Count -eq 1) { $avant = @() } else { $avant = @($avant[0..($avant.Count - 2)]) }
    }

    $nouveau = @($avant + '' + $section.Split("`n") + $apres) -join "`n"
    [System.IO.File]::WriteAllText($Chemin, $nouveau, [System.Text.UTF8Encoding]::new($false))

    Write-Host "Section insérée dans $Chemin." -ForegroundColor Green
    Write-Host "Rédige la partie « Détail » avec /nouvelle-changelog." -ForegroundColor DarkGray
}
finally {
    Pop-Location
}