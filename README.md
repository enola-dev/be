# Bee 🐝

An 🐣 _incubator_ for `be`.

Bee 🐝 is a Task/Action/Workflow engine & tool (CLI), which can be a build tool - but not only.

This is a part of and may eventually get integrated into [enola](https://github.com/enola-dev/enola) (or not; TBD).

## Development

This project uses [pre-commit](https://pre-commit.com/) to ensure code quality and consistency.

### Setting up pre-commit

Install pre-commit (if not already installed):

```bash
pip install pre-commit
```

Install the git hook scripts:

```bash
pre-commit install
```

Now pre-commit will run automatically on `git commit`. You can also run it manually on all files:

```bash
pre-commit run --all-files
```

The repository uses [palantir-java-format](https://github.com/palantir/palantir-java-format) to format Java code.
