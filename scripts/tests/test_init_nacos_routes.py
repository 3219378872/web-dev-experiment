import ast
import json
from pathlib import Path


def _load_seeded_routes() -> list[dict]:
    script = Path(__file__).resolve().parents[1] / "init-nacos-routes.sh"
    contents = script.read_text(encoding="utf-8")
    prefix = "ROUTES='"
    start = contents.index(prefix) + len(prefix)
    end = contents.index("'\n\nENCODED=", start)
    return json.loads(ast.literal_eval(repr(contents[start:end])))


def test_item_service_routes_admin_categories():
    routes = _load_seeded_routes()
    item_route = next(route for route in routes if route["id"] == "item-service")
    path_args = item_route["predicates"][0]["args"].values()

    assert "/admin/categories" in path_args
    assert "/admin/categories/**" in path_args
